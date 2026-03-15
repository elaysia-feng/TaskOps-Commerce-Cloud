package com.elias.aiproxy.service;

import com.elias.aiproxy.dto.req.AiChatRequest;
import com.elias.aiproxy.dto.resp.AiChatPrepareResponse;
import reactor.core.publisher.Flux;

public interface AiChatService {

    AiChatPrepareResponse prepareChat(Long userId, AiChatRequest request);

    Flux<String> streamChat(Long userId, AiChatRequest request);
}
