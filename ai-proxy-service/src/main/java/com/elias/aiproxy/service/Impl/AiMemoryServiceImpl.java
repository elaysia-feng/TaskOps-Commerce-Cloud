package com.elias.aiproxy.service.impl;

import com.elias.aiproxy.dto.req.SaveMemoryRequest;
import com.elias.aiproxy.dto.resp.AiMemoryResponse;
import com.elias.aiproxy.mapper.AiMemoryMapper;
import com.elias.aiproxy.mapper.AiSessionMemoryRelMapper;
import com.elias.aiproxy.service.AiMemoryService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AiMemoryServiceImpl implements AiMemoryService {

    private final AiMemoryMapper aiMemoryMapper;
    private final AiSessionMemoryRelMapper aiSessionMemoryRelMapper;

    public AiMemoryServiceImpl(AiMemoryMapper aiMemoryMapper,
                               AiSessionMemoryRelMapper aiSessionMemoryRelMapper) {
        this.aiMemoryMapper = aiMemoryMapper;
        this.aiSessionMemoryRelMapper = aiSessionMemoryRelMapper;
    }

    @Override
    public List<AiMemoryResponse> listUserMemories(Long userId) {
        return Collections.emptyList();
    }

    @Override
    public Long saveMemory(Long userId, SaveMemoryRequest request) {
        throw new UnsupportedOperationException("TODO: 实现记忆新增/编辑逻辑");
    }

    @Override
    public void deleteMemory(Long userId, Long memoryId) {
        throw new UnsupportedOperationException("TODO: 实现记忆删除逻辑");
    }
}
