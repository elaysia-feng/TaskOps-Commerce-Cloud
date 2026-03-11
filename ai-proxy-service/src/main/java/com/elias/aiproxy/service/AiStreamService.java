package com.elias.aiproxy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class AiStreamService {

    private final WebClient springAiWebClient;

    public Flux<String> streamChat(String prompt, String chatId) {
        return springAiWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/ai/chat")
                        .queryParam("prompt", prompt)
                        .queryParam("ChatId", chatId)
                        .build())
                .accept(MediaType.TEXT_HTML)
                .retrieve()
                .bodyToFlux(String.class);
    }
}