package com.elias.aiproxy.service.impl;

import com.elias.aiproxy.dto.req.AiChatRequest;
import com.elias.aiproxy.dto.resp.AiChatPrepareResponse;
import com.elias.aiproxy.mapper.AiMessageMapper;
import com.elias.aiproxy.mapper.AiSessionMapper;
import com.elias.aiproxy.mapper.AiSessionMemoryRelMapper;
import com.elias.aiproxy.mapper.AiToolCallLogMapper;
import com.elias.aiproxy.service.AiChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiChatServiceImpl implements AiChatService {

    private final ChatClient chatClient;
    private final AiSessionMapper aiSessionMapper;
    private final AiMessageMapper aiMessageMapper;
    private final AiSessionMemoryRelMapper aiSessionMemoryRelMapper;
    private final AiToolCallLogMapper aiToolCallLogMapper;

    public AiChatServiceImpl(ChatClient chatClient,
                             AiSessionMapper aiSessionMapper,
                             AiMessageMapper aiMessageMapper,
                             AiSessionMemoryRelMapper aiSessionMemoryRelMapper,
                             AiToolCallLogMapper aiToolCallLogMapper) {
        this.chatClient = chatClient;
        this.aiSessionMapper = aiSessionMapper;
        this.aiMessageMapper = aiMessageMapper;
        this.aiSessionMemoryRelMapper = aiSessionMemoryRelMapper;
        this.aiToolCallLogMapper = aiToolCallLogMapper;
    }

    @Override
    public AiChatPrepareResponse prepareChat(Long userId, AiChatRequest request) {
        throw new UnsupportedOperationException("TODO: chatId为空时自动创建会话并生成标题");
    }

    @Override
    public Flux<String> streamChat(Long userId, AiChatRequest request) {
        return Flux.error(new UnsupportedOperationException("TODO: 实现流式聊天逻辑"));
    }
}
