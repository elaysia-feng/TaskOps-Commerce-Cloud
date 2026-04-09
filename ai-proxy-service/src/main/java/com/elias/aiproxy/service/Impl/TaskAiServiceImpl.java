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
        String conversationId = createConversationId();
        return streamTaskAnswer(question, conversationId);
    }

    private String createConversationId() {
        return UUID.randomUUID().toString();
    }

    private Flux<String> streamTaskAnswer(String question, String conversationId) {
        return chatClient
                .prompt()
                .user(question)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }
}
