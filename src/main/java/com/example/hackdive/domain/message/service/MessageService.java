package com.example.hackdive.domain.message.service;

import com.example.hackdive.domain.message.dto.GptRequestDTO;
import com.example.hackdive.domain.message.dto.MessageInput;
import com.example.hackdive.domain.message.entity.Message;
import com.example.hackdive.domain.message.repository.MessageRepository;
import com.example.hackdive.domain.workspace.entity.Workspace;
import com.example.hackdive.domain.workspace.repository.WorkspaceRepository;
import com.example.hackdive.global.cofig.GPTConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final WorkspaceRepository workspaceRepository;

    @Value("${openai.api.key}")
    private String apiKey;

    public MessageService(MessageRepository messageRepository, WorkspaceRepository workspaceRepository) {
        this.messageRepository = messageRepository;
        this.workspaceRepository = workspaceRepository;
    }

    public void saveMessage(MessageInput message) {
        Workspace workspace = workspaceRepository.findById(message.getWorkspaceId())
                .orElseThrow(() -> new RuntimeException("No workspace id " + message.getWorkspaceId()));

        Message newMessage = Message.builder()
                .workspace(workspace)
                .role(message.getRole())
                .content(message.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        messageRepository.save(newMessage);
    }

    public List<Message> getAllMessage(Long workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("No workspace id " + workspaceId));

        return messageRepository.findAllByWorkspaceOrderByCreatedAtDesc(workspace);
    }

    public List<Message> getLLMInputs(Workspace workspace, boolean isFirst) {
        List<Message> messages = messageRepository.findAllByWorkspaceOrderByCreatedAtDesc(workspace);
        if (messages == null) {
            throw new RuntimeException("The Messages is Null");
        }

        List<Message> parsedDatas = new ArrayList<>();
        Message systemMessage = Message.builder()
                .createdAt(LocalDateTime.now())
                .content(GPTConfig.getSystemPrompts(isFirst))
                .role("assistant")
                .workspace(workspace)
                .build();
        parsedDatas.add(systemMessage);

        if (!isFirst) {
            for (int i = messages.size() - 1; i >= 0; i--) {
                parsedDatas.add(messages.get(i));
            }
        }
        return parsedDatas;
    }

    public String getGptOutputSync(Long workspaceId, boolean isFirst) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("No workspace id " + workspaceId));

        List<Message> inputMessages = getLLMInputs(workspace, isFirst);
        String gptResponse = getResponseSync(inputMessages);

        saveMessage(MessageInput.builder()
                .content(gptResponse)
                .role(GPTConfig.ROLE_ASSISTANT)
                .workspaceId(workspaceId)
                .build());

        return gptResponse;
    }

    public Flux<String> getResponse(List<Message> messages) {
        WebClient webClient = WebClient.builder()
                .baseUrl(GPTConfig.CHAT_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(GPTConfig.AUTHORIZATION, GPTConfig.BEARER + apiKey)
                .build();

        GptRequestDTO request = GptRequestDTO.builder()
                .model(GPTConfig.CHAT_MODEL)
                .maxTokens(GPTConfig.MAX_TOKEN)
                .temperature(GPTConfig.TEMPERATURE)
                .stream(GPTConfig.STREAM)
                .messages(messages)
                .build();

        return webClient.post()
                .bodyValue(request)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class);
    }

    public String extractContent(String jsonEvent) {
        if ("DONE".equals(jsonEvent)) {
            return "";
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(jsonEvent);
            return node.at("/choices/0/message/content").asText();
        } catch (IOException e) {
            System.err.println("Error processing JSON: " + e.getMessage());
            return "";
        }
    }

    public String getResponseSync(List<Message> messages) {
        WebClient webClient = WebClient.builder()
                .baseUrl(GPTConfig.CHAT_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(GPTConfig.AUTHORIZATION, GPTConfig.BEARER + apiKey)
                .build();

        GptRequestDTO request = GptRequestDTO.builder()
                .model(GPTConfig.CHAT_MODEL)
                .maxTokens(GPTConfig.MAX_TOKEN)
                .temperature(GPTConfig.TEMPERATURE)
                .stream(false)
                .messages(messages)
                .build();

        try {
            String response = webClient.post()
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return extractContent(response);
        } catch (Exception e) {
            throw new RuntimeException("Error getting GPT response: " + e.getMessage());
        }
    }

    public void addEvent(SseEmitter emitter, String content) throws IOException {
        SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event().data(content).name("message");
        emitter.send(eventBuilder);
    }

    public Flux<String> streamMessages(Long workspaceId, boolean isFirst) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("No workspace id " + workspaceId));

        List<Message> inputMessages = getLLMInputs(workspace, isFirst);

        return Flux.create(sink -> {
            Flux<String> eventStream = getResponse(inputMessages);
            eventStream.subscribe(
                    content -> {
                        try {
                            addEvent(new SseEmitter(), content);
                            sink.next(content);
                        } catch (IOException e) {
                            sink.error(e);
                        }
                    },
                    sink::error,
                    sink::complete);

            new SseEmitter().onTimeout(sink::complete);
        });
    }
}
