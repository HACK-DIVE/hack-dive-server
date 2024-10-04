package com.example.hackdive.domain.message.contoller;

import com.example.hackdive.domain.message.entity.Message;
import com.example.hackdive.domain.message.service.MessageService;
import com.example.hackdive.global.common.SuccessResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/message")
@AllArgsConstructor
public class MessageController {
    private final MessageService messageService;

    // AI 채팅 반환(stream)
    @GetMapping("/recieve/{workspaceId}/{isFirst}")
    public SseEmitter streamMessages(@PathVariable("workspaceId") Long workspaceId, @PathVariable("isFirst") boolean isFirst) {
        SseEmitter emitter = new SseEmitter();

        Flux<String> str = messageService.streamMessages(workspaceId, isFirst);

        str.subscribe(
                data -> {
                    try {
                        emitter.send(data);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                emitter::complete
        );
        return emitter;
    }

    // 유저 채팅 전송
    @PostMapping("/send/{workspaceId}")
    public ResponseEntity<SuccessResponse<?>> sendMessage(@PathVariable Long workspaceId, @RequestBody String message) throws JsonProcessingException {
        messageService.saveMessage(workspaceId, message);
        return SuccessResponse.ok("");
    }

    // 채팅 조회
    @GetMapping("/{workspaceId}")
    public ResponseEntity<SuccessResponse<?>> getChatPage(@PathVariable("workspaceId") Long workspaceId) {
        List<Message> messages = messageService.getAllMessage(workspaceId);
        return SuccessResponse.ok(messages);
    }


    // AI 채팅 반환(sync)
    /*
    @GetMapping("/sync/{workspaceId}/{isFirst}")
    public String getGptOutputSync(@PathVariable("workspaceId") Long workspaceId, @PathVariable("isFirst") boolean isFirst) {
        return messageService.getGptOutputSync(workspaceId, isFirst);
    }
    */

}
