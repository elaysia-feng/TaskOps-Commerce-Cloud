package com.elias.wallet.service.impl;

import com.elias.wallet.service.WalletFlowService;
import com.elias.wallet.vo.PageResponse;
import com.elias.wallet.vo.WalletFlowVO;
import org.springframework.stereotype.Service;

@Service
public class WalletFlowServiceImpl implements WalletFlowService {

    @Override
    public PageResponse<WalletFlowVO> pageUserFlows(Long userId, String flowType, long pageNum, long pageSize) {
        throw new UnsupportedOperationException("TODO: implement wallet flow page query");
    }
}