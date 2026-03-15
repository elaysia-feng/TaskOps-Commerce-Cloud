package com.elias.aiproxy.service;

import com.elias.aiproxy.dto.req.CreateSessionRequest;
import com.elias.aiproxy.dto.req.SessionMemorySelectRequest;
import com.elias.aiproxy.dto.resp.AiMemoryResponse;
import com.elias.aiproxy.dto.resp.AiMessageResponse;
import com.elias.aiproxy.dto.resp.AiSessionSummaryResponse;
import com.elias.aiproxy.dto.resp.CreateSessionResponse;
import com.elias.aiproxy.entity.AiSession;

import java.util.List;

public interface AiSessionService {

    CreateSessionResponse createSession(Long userId, CreateSessionRequest request);

    List<AiSessionSummaryResponse> listSessions(Long userId, String sessionType);

    List<AiMessageResponse> listSessionMessages(Long userId, String chatId);

    List<AiMemoryResponse> listSessionMemories(Long userId, String chatId);

    void changeMemorySelection(Long userId, SessionMemorySelectRequest request);

    AiSession requireOwnedSession(Long userId, String chatId);
}
