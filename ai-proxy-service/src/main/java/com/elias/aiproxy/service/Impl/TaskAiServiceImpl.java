package com.elias.aiproxy.service.Impl;

import com.elias.aiproxy.service.TaskAiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
public class TaskAiServiceImpl implements TaskAiService {
    private final ChatClient chatClient;

    public TaskAiServiceImpl(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Flux<String> chat(String question) {
        String userId = UUID.randomUUID().toString();
        // 把会话id放入mysql库中

        return chatClient
                /// 开始构建一次 Prompt / 请求
                .prompt()
                ///
                .user(question)
                .advisors(s -> {s.param(ChatMemory.CONVERSATION_ID, userId);})
                .stream()
                .content();
    }
}
