package com.elias.aiproxy.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elias.aiproxy.contents.TaskAiContents;
import com.elias.aiproxy.dto.req.AiChatRequest;
import com.elias.aiproxy.dto.resp.AiChatPrepareResponse;
import com.elias.aiproxy.entity.AiMessage;
import com.elias.aiproxy.entity.AiSession;
import com.elias.aiproxy.mapper.AiMessageMapper;
import com.elias.aiproxy.mapper.AiSessionMapper;
import com.elias.aiproxy.mapper.AiSessionMemoryRelMapper;
import com.elias.aiproxy.mapper.AiToolCallLogMapper;
import com.elias.aiproxy.service.AiChatService;
import com.elias.aiproxy.tool.TaskOpsTools;
import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AiChatServiceImpl implements AiChatService {

    private static final String DEFAULT_CHAT_TITLE = "New Chat";
    private static final String DEFAULT_MODEL_NAME = "qwen-flash";

    private final ChatClient chatClient;
    private final AiSessionMapper aiSessionMapper;
    private final AiMessageMapper aiMessageMapper;
    private final AiSessionMemoryRelMapper aiSessionMemoryRelMapper;
    private final AiToolCallLogMapper aiToolCallLogMapper;
    private final TaskOpsTools taskOpsTools;

    public AiChatServiceImpl(ChatClient chatClient,
                             AiSessionMapper aiSessionMapper,
                             AiMessageMapper aiMessageMapper,
                             AiSessionMemoryRelMapper aiSessionMemoryRelMapper,
                             AiToolCallLogMapper aiToolCallLogMapper,
                             TaskOpsTools taskOpsTools) {
        this.chatClient = chatClient;
        this.aiSessionMapper = aiSessionMapper;
        this.aiMessageMapper = aiMessageMapper;
        this.aiSessionMemoryRelMapper = aiSessionMemoryRelMapper;
        this.aiToolCallLogMapper = aiToolCallLogMapper;
        this.taskOpsTools = taskOpsTools;
    }

    @Override
    public AiChatPrepareResponse prepareChat(Long userId, AiChatRequest request) {
        validatePromptRequest(request);
        String chatId = resolveChatId(request);
        if (chatId != null) {
            AiSession session = loadOwnedSession(userId, chatId);
            return buildPreparedChatResponse(session, 0);
        }
        return createPendingSession(userId);
    }

    @Override
    public Flux<String> streamChat(Long userId, AiChatRequest request) {
        validatePromptRequest(request);
        StreamChatContext context = prepareStreamChatContext(userId, request);
        saveUserMessage(context);
        touchSession(context.getSession(), context.getUserMessageAt());
        return streamAssistantReply(context);
    }

    private String resolveChatId(AiChatRequest request) {
        return trimToNull(request.getChatId());
    }

    private AiChatPrepareResponse buildPreparedChatResponse(AiSession session, Integer newlyCreated) {
        return new AiChatPrepareResponse(session.getChatId(), session.getTitle(), newlyCreated);
    }

    private AiChatPrepareResponse createPendingSession(Long userId) {
        String chatId = java.util.UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();

        AiSession session = new AiSession();
        session.setChatId(chatId);
        session.setUserId(userId);
        session.setTitle(DEFAULT_CHAT_TITLE);
        session.setSessionType("chat");
        session.setModelName(DEFAULT_MODEL_NAME);
        session.setMemoryEnabled(1);
        session.setStatus(1);
        session.setLastMessageAt(now);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        aiSessionMapper.insert(session);

        return buildPreparedChatResponse(session, 1);
    }

    private StreamChatContext prepareStreamChatContext(Long userId, AiChatRequest request) {
        AiChatPrepareResponse prepared = prepareChat(userId, request);
        AiSession session = loadOwnedSession(userId, prepared.getChatId());

        StreamChatContext context = new StreamChatContext();
        context.setChatId(prepared.getChatId());
        context.setPrompt(request.getPrompt().trim());
        context.setSession(session);
        context.setUserMessageAt(LocalDateTime.now());
        context.setAssistantReplyRef(new AtomicReference<>(new StringBuilder()));
        return context;
    }

    private void saveUserMessage(StreamChatContext context) {
        saveMessage(
                context.getChatId(),
                "user",
                context.getPrompt(),
                nextSeqNo(context.getChatId()),
                context.getSession().getModelName(),
                context.getUserMessageAt()
        );
    }

    private Flux<String> streamAssistantReply(StreamChatContext context) {
        return chatClient.prompt()
                .user(context.getPrompt())
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, context.getChatId()))
                .tools(taskOpsTools)
                .stream()
                .content()
                .doOnNext(chunk -> context.getAssistantReplyRef().get().append(chunk))
                .doOnComplete(() -> handleAssistantReplyCompleted(context));
    }

    private void handleAssistantReplyCompleted(StreamChatContext context) {
        String assistantReply = context.getAssistantReplyRef().get().toString().trim();
        LocalDateTime finishedAt = LocalDateTime.now();

        saveMessage(
                context.getChatId(),
                "assistant",
                assistantReply,
                nextSeqNo(context.getChatId()),
                context.getSession().getModelName(),
                finishedAt
        );
        touchSession(context.getSession(), finishedAt);
        updateTitleIfNeeded(context.getSession(), context.getPrompt(), assistantReply);
    }

    private AiSession loadOwnedSession(Long userId, String chatId) {
        AiSession session = aiSessionMapper.selectOne(new LambdaQueryWrapper<AiSession>()
                .eq(AiSession::getChatId, chatId)
                .eq(AiSession::getUserId, userId)
                .eq(AiSession::getStatus, 1)
                .last("limit 1"));
        if (session == null) {
            throw new IllegalArgumentException("chat session not found or access denied");
        }
        return session;
    }

    private void saveMessage(String chatId,
                             String role,
                             String content,
                             Integer seqNo,
                             String modelName,
                             LocalDateTime createdAt) {
        AiMessage message = new AiMessage();
        message.setChatId(chatId);
        message.setRole(role);
        message.setContent(content);
        message.setSeqNo(seqNo);
        message.setMessageType("text");
        message.setModelName(modelName);
        message.setPromptTokens(0);
        message.setCompletionTokens(0);
        message.setTotalTokens(0);
        message.setStatus(1);
        message.setCreatedAt(createdAt);
        aiMessageMapper.insert(message);
    }

    private Integer nextSeqNo(String chatId) {
        Long count = aiMessageMapper.selectCount(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getChatId, chatId));
        return count == null ? 1 : count.intValue() + 1;
    }

    private void touchSession(AiSession session, LocalDateTime updatedAt) {
        AiSession update = new AiSession();
        update.setId(session.getId());
        update.setLastMessageAt(updatedAt);
        update.setUpdatedAt(updatedAt);
        aiSessionMapper.updateById(update);
        session.setLastMessageAt(updatedAt);
        session.setUpdatedAt(updatedAt);
    }

    private void updateTitleIfNeeded(AiSession session, String prompt, String answer) {
        if (!isDefaultTitle(session.getTitle()) || answer == null || answer.isBlank()) {
            return;
        }

        String title = generateTitleByAi(prompt, answer);
        AiSession update = new AiSession();
        update.setId(session.getId());
        update.setTitle(title);
        update.setUpdatedAt(LocalDateTime.now());
        aiSessionMapper.updateById(update);
        session.setTitle(title);
    }

    private boolean isDefaultTitle(String title) {
        return title == null || title.trim().isEmpty() || DEFAULT_CHAT_TITLE.equals(title.trim());
    }

    private String generateTitleByAi(String prompt, String answer) {
        String raw = chatClient.prompt()
                .system(TaskAiContents.TITLE_PROMPT)
                .user("User prompt:\n" + prompt + "\n\nAssistant answer:\n" + answer)
                .call()
                .content();
        if (raw == null || raw.trim().isEmpty()) {
            return DEFAULT_CHAT_TITLE;
        }

        String title = raw.trim()
                .replace("\n", "")
                .replace("\r", "")
                .replace(":", "");
        return title.length() > 12 ? title.substring(0, 12) : title;
    }

    private void validatePromptRequest(AiChatRequest request) {
        if (request == null || trimToNull(request.getPrompt()) == null) {
            throw new IllegalArgumentException("prompt can not be blank");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Data
    private static class StreamChatContext {
        private String chatId;
        private String prompt;
        private AiSession session;
        private LocalDateTime userMessageAt;
        private AtomicReference<StringBuilder> assistantReplyRef;
    }
}
