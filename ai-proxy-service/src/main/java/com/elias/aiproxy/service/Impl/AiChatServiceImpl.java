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
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AiChatServiceImpl implements AiChatService {

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
        // 验证会话内容是否为空
        validatePrompt(request);
        // 检验会话id是否为空
        String chatId = trimToNull(request.getChatId());
        if (chatId != null) {
            // 根据useId和会话id查询
            AiSession session = requireOwnedSession(userId, chatId);
            return new AiChatPrepareResponse(session.getChatId(), session.getTitle(), 0);
        }
        // 创建新的会话
        return createPendingSession(userId);
    }

    @Override
    public Flux<String> streamChat(Long userId, AiChatRequest request) {
        // 验证会话内容是否为空
        validatePrompt(request);
        // 准备对话
        AiChatPrepareResponse prepared = prepareChat(userId, request);
        String chatId = prepared.getChatId();
        String prompt = request.getPrompt().trim();
        // 查询会话
        AiSession session = requireOwnedSession(userId, chatId);
        LocalDateTime now = LocalDateTime.now();
        /*
         * 保存信息
         * nextSeqNo -> 查询有多少会话条数, 然后返回记录条数
         */
        saveMessage(chatId, "user", prompt, nextSeqNo(chatId), session.getModelName(), now);
        // 更新会话session里面的跟新时间这些
        touchSession(session, now);
        // 这个是为了在 Flux 流式回调里“累计 AI 的完整回复内容”
        AtomicReference<StringBuilder> assistantReplyRef = new AtomicReference<>(new StringBuilder());

        return chatClient.prompt()
                .user(prompt)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .tools(taskOpsTools)
                .stream()
                .content()
                //.doOnNext(...)：每收到一小段流式内容时做点事
                //
                //.doOnComplete(...)：整个流结束时做点事
                .doOnNext(chunk -> assistantReplyRef.get().append(chunk))
                .doOnComplete(() -> {
                    //拿到的就是“AI 这一次完整回复的最终文本”
                    String assistantReply = assistantReplyRef.get().toString().trim();
                    LocalDateTime finishedAt = LocalDateTime.now();
                    saveMessage(chatId, "assistant", assistantReply, nextSeqNo(chatId), session.getModelName(), finishedAt);
                    touchSession(session, finishedAt);
                    updateTitleIfNeeded(session, prompt, assistantReply);
                });
    }

    private AiChatPrepareResponse createPendingSession(Long userId) {
        String chatId = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();

        AiSession session = new AiSession();
        session.setChatId(chatId);
        session.setUserId(userId);
        session.setTitle("新对话");
        session.setSessionType("chat");
        session.setModelName("qwen-flash");
        session.setMemoryEnabled(1);
        session.setStatus(1);
        session.setLastMessageAt(now);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        aiSessionMapper.insert(session);

        return new AiChatPrepareResponse(chatId, session.getTitle(), 1);
    }

    private AiSession requireOwnedSession(Long userId, String chatId) {
        AiSession session = aiSessionMapper.selectOne(new LambdaQueryWrapper<AiSession>()
                .eq(AiSession::getChatId, chatId)
                .eq(AiSession::getUserId, userId)
                .eq(AiSession::getStatus, 1)
                .last("limit 1"));
        if (session == null) {
            throw new IllegalArgumentException("会话不存在或无权访问");
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
        // 查当前会话有多少条消息
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
        return title == null || title.trim().isEmpty() || "新对话".equals(title.trim());
    }

    private String generateTitleByAi(String prompt, String answer) {
        String raw = chatClient.prompt()
                .system(TaskAiContents.TITLE_PROMPT)
                .user("用户问题：\n" + prompt + "\n\n助手回答：\n" + answer)
                .call()
                .content();
        if (raw == null || raw.trim().isEmpty()) {
            return "新对话";
        }

        String title = raw.trim()
                .replace("\n", "")
                .replace("\r", "")
                .replace("：", "")
                .replace(":", "");
        return title.length() > 12 ? title.substring(0, 12) : title;
    }

    // 验证问题是否为空
    private void validatePrompt(AiChatRequest request) {
        if (request == null || request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            throw new IllegalArgumentException("prompt不能为空");
        }
    }

    // 校验值是否为空
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        // 去掉字符串首尾的空白字符
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
