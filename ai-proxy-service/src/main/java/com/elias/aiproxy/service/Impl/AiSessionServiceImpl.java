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
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class AiSessionServiceImpl implements AiSessionService {

    private static final String DEFAULT_CHAT_TITLE = "New Chat";
    private static final String DEFAULT_SESSION_TYPE = "chat";
    private static final String DEFAULT_MODEL_NAME = "qwen-plus";

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
        CreateSessionContext context = prepareCreateSessionContext(userId, request);
        executeSessionCreation(context);
        return buildCreateSessionResponse(context);
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
        throw new UnsupportedOperationException("TODO: implement session memory selection");
    }

    @Override
    public AiSession requireOwnedSession(Long userId, String chatId) {
        throw new UnsupportedOperationException("TODO: implement session ownership validation");
    }

    private CreateSessionContext prepareCreateSessionContext(Long userId, CreateSessionRequest request) {
        String chatId = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();

        AiSession session = new AiSession();
        session.setChatId(chatId);
        session.setUserId(userId);
        session.setTitle(DEFAULT_CHAT_TITLE);
        session.setSessionType(defaultIfBlank(request == null ? null : request.getSessionType(), DEFAULT_SESSION_TYPE));
        session.setModelName(defaultIfBlank(request == null ? null : request.getModelName(), DEFAULT_MODEL_NAME));
        session.setMemoryEnabled(request == null || request.getMemoryEnabled() == null ? 1 : request.getMemoryEnabled());
        session.setStatus(1);
        session.setLastMessageAt(now);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);

        CreateSessionContext context = new CreateSessionContext();
        context.setChatId(chatId);
        context.setSession(session);
        return context;
    }

    private void executeSessionCreation(CreateSessionContext context) {
        aiSessionMapper.insert(context.getSession());
    }

    private CreateSessionResponse buildCreateSessionResponse(CreateSessionContext context) {
        return new CreateSessionResponse(context.getChatId());
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    @Data
    private static class CreateSessionContext {
        private String chatId;
        private AiSession session;
    }
}
