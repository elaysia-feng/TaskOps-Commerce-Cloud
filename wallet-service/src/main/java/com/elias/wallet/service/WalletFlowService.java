package com.elias.wallet.service;

import com.elias.wallet.vo.PageResponse;
import com.elias.wallet.vo.WalletFlowVO;

public interface WalletFlowService {

    PageResponse<WalletFlowVO> pageUserFlows(Long userId, String flowType, long pageNum, long pageSize);
}