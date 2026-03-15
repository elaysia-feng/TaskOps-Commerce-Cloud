package com.elias.aiproxy.controller;

import com.elias.aiproxy.dto.req.AiChatRequest;
import com.elias.aiproxy.dto.req.CreateSessionRequest;
import com.elias.aiproxy.dto.req.SaveMemoryRequest;
import com.elias.aiproxy.dto.req.SessionMemorySelectRequest;
import com.elias.aiproxy.dto.resp.AiChatPrepareResponse;
import com.elias.aiproxy.dto.resp.AiMemoryResponse;
import com.elias.aiproxy.dto.resp.AiMessageResponse;
import com.elias.aiproxy.dto.resp.CreateSessionResponse;
import com.elias.aiproxy.service.AiChatService;
import com.elias.aiproxy.service.AiMemoryService;
import com.elias.aiproxy.service.AiSessionService;
import com.elias.common.ApiResponse;
import com.elias.common.context.UserContext;
import com.elias.common.exception.BizException;
import com.elias.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai-proxy")
@Tag(name = "AI Proxy API", description = "AI 会话、记忆与流式聊天接口")
public class AiProxyController {

    private final AiSessionService aiSessionService;
    private final AiChatService aiChatService;
    private final AiMemoryService aiMemoryService;

    public AiProxyController(AiSessionService aiSessionService,
                             AiChatService aiChatService,
                             AiMemoryService aiMemoryService) {
        this.aiSessionService = aiSessionService;
        this.aiChatService = aiChatService;
        this.aiMemoryService = aiMemoryService;
    }

    @PostMapping("/session")
    @Operation(summary = "创建 AI 会话")
    public ApiResponse<CreateSessionResponse> createSession(
            @RequestParam(value = "type", defaultValue = "chat") String type) {
        Long userId = requireLogin();
        CreateSessionRequest request = new CreateSessionRequest();
        request.setSessionType(type);
        return ApiResponse.ok(aiSessionService.createSession(userId, request));
    }

    @GetMapping("/history/{type}")
    @Operation(summary = "查询指定类型的会话列表")
    public ApiResponse<List<String>> listSessions(
            @Parameter(description = "会话类型", required = true)
            @PathVariable("type") String type) {
        Long userId = requireLogin();
        List<String> chatIds = aiSessionService.listSessions(userId, type)
                .stream()
                .map(item -> item.getChatId())
                .collect(Collectors.toList());
        return ApiResponse.ok(chatIds);
    }

    @GetMapping("/history/session/{chatId}")
    @Operation(summary = "查询单个会话的历史消息")
    public ApiResponse<List<AiMessageResponse>> getSessionHistory(
            @Parameter(description = "会话chatId", required = true)
            @PathVariable("chatId") String chatId) {
        Long userId = requireLogin();
        return ApiResponse.ok(aiSessionService.listSessionMessages(userId, chatId));
    }

    @GetMapping("/memory")
    @Operation(summary = "查询当前用户的长期记忆列表")
    public ApiResponse<List<AiMemoryResponse>> listMemories() {
        Long userId = requireLogin();
        return ApiResponse.ok(aiMemoryService.listUserMemories(userId));
    }

    @GetMapping("/memory/session/{chatId}")
    @Operation(summary = "查询指定会话的记忆选择情况")
    public ApiResponse<List<AiMemoryResponse>> listSessionMemories(
            @Parameter(description = "会话chatId", required = true)
            @PathVariable("chatId") String chatId) {
        Long userId = requireLogin();
        return ApiResponse.ok(aiSessionService.listSessionMemories(userId, chatId));
    }

    @PostMapping("/memory")
    @Operation(summary = "新增或编辑长期记忆")
    public ApiResponse<Long> saveMemory(@Valid @RequestBody SaveMemoryRequest request) {
        Long userId = requireLogin();
        return ApiResponse.ok(aiMemoryService.saveMemory(userId, request));
    }

    @PutMapping("/memory/select")
    @Operation(summary = "切换会话中某条记忆的选中状态")
    public ApiResponse<Void> changeMemorySelection(@Valid @RequestBody SessionMemorySelectRequest request) {
        Long userId = requireLogin();
        aiSessionService.changeMemorySelection(userId, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/memory/{memoryId}")
    @Operation(summary = "删除长期记忆")
    public ApiResponse<Void> deleteMemory(
            @Parameter(description = "记忆ID", required = true)
            @PathVariable("memoryId") @NotNull Long memoryId) {
        Long userId = requireLogin();
        aiMemoryService.deleteMemory(userId, memoryId);
        return ApiResponse.ok();
    }

    @PostMapping("/chat/prepare")
    @Operation(summary = "准备聊天会话，chatId为空时自动创建会话并生成标题")
    public ApiResponse<AiChatPrepareResponse> prepareChat(@Valid @RequestBody AiChatRequest request) {
        Long userId = requireLogin();
        return ApiResponse.ok(aiChatService.prepareChat(userId, request));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "基于会话的流式聊天")
    public Flux<String> streamChat(
            @RequestParam("prompt") @NotBlank String prompt,
            @RequestParam(value = "chatId", required = false) String chatId) {
        Long userId = requireLogin();
        AiChatRequest request = new AiChatRequest();
        request.setPrompt(prompt);
        request.setChatId(chatId);
        return aiChatService.streamChat(userId, request);
    }

    private Long requireLogin() {
        Long uid = UserContext.userId();
        if (uid == null) {
            throw new BizException(ErrorCode.NOT_LOGGED_IN);
        }
        return uid;
    }
}
