package com.elias.aiproxy.service;

import com.elias.aiproxy.dto.req.SaveMemoryRequest;
import com.elias.aiproxy.dto.resp.AiMemoryResponse;

import java.util.List;

public interface AiMemoryService {

    List<AiMemoryResponse> listUserMemories(Long userId);

    Long saveMemory(Long userId, SaveMemoryRequest request);

    void deleteMemory(Long userId, Long memoryId);
}
