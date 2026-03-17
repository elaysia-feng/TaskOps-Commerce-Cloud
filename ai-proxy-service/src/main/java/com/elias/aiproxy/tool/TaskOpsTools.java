package com.elias.aiproxy.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elias.aiproxy.entity.AiSession;
import com.elias.aiproxy.mapper.AiSessionMapper;
import com.elias.common.context.UserContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TaskOpsTools {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AiSessionMapper aiSessionMapper;

    public TaskOpsTools(AiSessionMapper aiSessionMapper) {
        this.aiSessionMapper = aiSessionMapper;
    }

    @Tool(description = "获取当前系统时间")
    public String getCurrentTime() {
        return "当前时间：" + LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    @Tool(description = "查询当前登录用户的AI会话统计信息")
    public String getMyAiSessionStats() {
        Long userId = UserContext.userId();
        if (userId == null) {
            return "当前未登录，无法查询AI会话统计";
        }

        Long total = aiSessionMapper.selectCount(new LambdaQueryWrapper<AiSession>()
                .eq(AiSession::getUserId, userId));

        Long active = aiSessionMapper.selectCount(new LambdaQueryWrapper<AiSession>()
                .eq(AiSession::getUserId, userId)
                .eq(AiSession::getStatus, 1));

        return "当前用户ID=" + userId
                + "，AI会话总数=" + safeCount(total)
                + "，有效会话数=" + safeCount(active);
    }

    private long safeCount(Long value) {
        return value == null ? 0L : value;
    }
}
