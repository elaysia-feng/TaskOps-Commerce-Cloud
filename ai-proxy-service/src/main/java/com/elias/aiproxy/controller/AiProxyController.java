package com.elias.aiproxy.controller;

import com.elias.aiproxy.service.TaskAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai-proxy")
@RequiredArgsConstructor
public class AiProxyController {

    private final TaskAiService taskAiService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(String question) {
         return taskAiService.chat(question);
    }
}