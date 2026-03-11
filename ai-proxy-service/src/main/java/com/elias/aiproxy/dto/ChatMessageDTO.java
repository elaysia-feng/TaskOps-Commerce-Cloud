package com.elias.aiproxy.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private String role;
    private String content;
}