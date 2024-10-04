package com.example.hackdive.domain.message.contoller;

import com.example.hackdive.domain.message.dto.MessageInput;
import com.example.hackdive.domain.message.entity.Message;
import com.example.hackdive.domain.message.service.MessageService;
import com.example.hackdive.global.common.SuccessResponse;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/message")
@AllArgsConstructor
public class MessageController {
    private final MessageService messageService;

    // AI 채팅 반환(stream)
    @GetMapping("/stream/{workspaceId}/{isFirst}")
    public SseEmitter streamMessages(@PathVariable Long workspaceId, @PathVariable boolean isFirst) {
        SseEmitter emitter = new SseEmitter(60000L);

        CompletableFuture.runAsync(() -> {
            try {
                messageService.streamMessages(workspaceId, isFirst)
                        .subscribe(
                                content -> {
                                    try {
                                        messageService.addEvent(emitter, content);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                },
                                emitter::completeWithError,
                                emitter::complete
                        );
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }


    // AI 채팅 반환
    @GetMapping("/sync/{workspaceId}/{isFirst}")
    public String getGptOutputSync(@PathVariable("workspaceId") Long workspaceId, @PathVariable("isFirst") boolean isFirst) {
        return messageService.getGptOutputSync(workspaceId, isFirst);
    }

    // 유저 채팅 전송
    @PostMapping("/send")
    public ResponseEntity<SuccessResponse<?>> sendMessage (@RequestBody MessageInput message) {
        messageService.saveMessage(message);
        return SuccessResponse.ok("");
    }

    // 채팅 조회
    @GetMapping("/{workspaceId}")
    public ResponseEntity<SuccessResponse<?>> getChatPage(@PathVariable("workspaceId") Long workspaceId) {
        List<Message> messages = messageService.getAllMessage(workspaceId);
        return SuccessResponse.ok(messages);
    }

}
