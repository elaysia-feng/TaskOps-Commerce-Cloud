package com.elias.aiproxy.controller;

import com.elias.aiproxy.client.SpringAiClient;
import com.elias.aiproxy.dto.AiChatRequest;
import com.elias.aiproxy.dto.ChatMessageDTO;
import com.elias.aiproxy.service.AiStreamService;
import com.elias.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-proxy")
@RequiredArgsConstructor
public class AiProxyController {

    private final SpringAiClient springAiClient;
    private final AiStreamService aiStreamService;

    @PostMapping("/chat")
    public ApiResponse<Map<String, String>> chat(@RequestBody AiChatRequest request) {
        String content = springAiClient.chat(request.getPrompt(), request.getChatId());
        Map<String, String> result = new LinkedHashMap<>();
        result.put("chatId", request.getChatId());
        result.put("content", content);
        return ApiResponse.ok(result);
    }

    @GetMapping(value = "/stream", produces = "text/html;charset=utf-8")
    public Flux<String> stream(@RequestParam String prompt, @RequestParam String chatId) {
        return aiStreamService.streamChat(prompt, chatId);
    }

    @PostMapping("/session")
    public ApiResponse<Map<String, String>> createSession(@RequestParam(defaultValue = "chat") String type) {
        return ApiResponse.ok(springAiClient.createSession(type));
    }

    @GetMapping("/history/{type}")
    public ApiResponse<List<String>> history(@PathVariable String type) {
        return ApiResponse.ok(springAiClient.getHistory(type));
    }

    @GetMapping("/history/session/{chatId}")
    public ApiResponse<List<ChatMessageDTO>> sessionHistory(@PathVariable String chatId) {
        return ApiResponse.ok(springAiClient.getChatHistory(chatId));
    }
}