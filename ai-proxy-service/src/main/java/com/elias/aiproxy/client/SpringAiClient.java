package com.elias.aiproxy.client;

import com.elias.aiproxy.dto.ChatMessageDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "spring-ai-client", url = "${spring-ai.service.url}")
public interface SpringAiClient {

    @GetMapping(value = "/api/ai/chat")
    String chat(@RequestParam("prompt") String prompt, @RequestParam("ChatId") String chatId);

    @GetMapping("/api/history/{type}")
    List<String> getHistory(@PathVariable("type") String type);

    @PostMapping("/api/history/session")
    Map<String, String> createSession(@RequestParam("type") String type);

    @GetMapping("/api/history/history/{chatId}")
    List<ChatMessageDTO> getChatHistory(@PathVariable("chatId") String chatId);
}