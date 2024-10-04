package com.example.hackdive.domain.message.dto;

import com.example.hackdive.global.cofig.GPTConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.example.hackdive.domain.message.entity.Message;

import java.util.List;

@Getter
@NoArgsConstructor
public class GptRequestDTO {
    private String model;
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    private Double temperature;
    private Boolean stream;
    private List<Message> messages;

    @Builder
    public GptRequestDTO(String model, Integer maxTokens, Double temperature, Boolean stream, List<Message> messages) {
        this.model = model;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
        this.stream = stream;
        this.messages = messages;
    }


}
