package com.elias.aiproxy.service.Impl;

import com.elias.aiproxy.dto.req.CreateSessionRequest;
import com.elias.aiproxy.dto.req.SessionMemorySelectRequest;
import com.elias.aiproxy.dto.resp.AiMemoryResponse;
import com.elias.aiproxy.dto.resp.AiMessageResponse;
import com.elias.aiproxy.dto.resp.AiSessionSummaryResponse;
import com.elias.aiproxy.dto.resp.CreateSessionResponse;
import com.elias.aiproxy.entity.AiSession;
import com.elias.aiproxy.mapper.AiMemoryMapper;
import com.elias.aiproxy.mapper.AiMessageMapper;
import com.elias.aiproxy.mapper.AiSessionMapper;
import com.elias.aiproxy.mapper.AiSessionMemoryRelMapper;
import com.elias.aiproxy.service.AiSessionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


@Service
public class AiSessionServiceImpl implements AiSessionService {

    private final AiSessionMapper aiSessionMapper;
    private final AiMessageMapper aiMessageMapper;
    private final AiMemoryMapper aiMemoryMapper;
    private final AiSessionMemoryRelMapper aiSessionMemoryRelMapper;

    public AiSessionServiceImpl(AiSessionMapper aiSessionMapper,
                                AiMessageMapper aiMessageMapper,
                                AiMemoryMapper aiMemoryMapper,
                                AiSessionMemoryRelMapper aiSessionMemoryRelMapper) {
        this.aiSessionMapper = aiSessionMapper;
        this.aiMessageMapper = aiMessageMapper;
        this.aiMemoryMapper = aiMemoryMapper;
        this.aiSessionMemoryRelMapper = aiSessionMemoryRelMapper;
    }

    @Override
    public CreateSessionResponse createSession(Long userId, CreateSessionRequest request) {
        String chatId = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();

        AiSession session = new AiSession();
        session.setChatId(chatId);
        session.setUserId(userId);
        session.setTitle("新对话");
        session.setSessionType(defaultIfBlank(request == null ? null : request.getSessionType(), "chat"));
        session.setModelName(defaultIfBlank(request == null ? null : request.getModelName(), "qwen-plus"));
        session.setMemoryEnabled(request == null || request.getMemoryEnabled() == null ? 1 : request.getMemoryEnabled());
        session.setStatus(1);
        session.setLastMessageAt(now);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);

        aiSessionMapper.insert(session);
        return new CreateSessionResponse(chatId);
    }

    @Override
    public List<AiSessionSummaryResponse> listSessions(Long userId, String sessionType) {
        return Collections.emptyList();
    }

    @Override
    public List<AiMessageResponse> listSessionMessages(Long userId, String chatId) {
        return Collections.emptyList();
    }

    @Override
    public List<AiMemoryResponse> listSessionMemories(Long userId, String chatId) {
        return Collections.emptyList();
    }

    @Override
    public void changeMemorySelection(Long userId, SessionMemorySelectRequest request) {
        throw new UnsupportedOperationException("TODO: 实现会话记忆勾选逻辑");
    }

    @Override
    public AiSession requireOwnedSession(Long userId, String chatId) {
        throw new UnsupportedOperationException("TODO: 实现会话归属校验逻辑");
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
