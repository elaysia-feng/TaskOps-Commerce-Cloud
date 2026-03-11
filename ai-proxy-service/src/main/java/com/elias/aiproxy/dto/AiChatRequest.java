package com.elias.aiproxy.dto;

import lombok.Data;

@Data
public class AiChatRequest {
    private String prompt;
    private String chatId;
}