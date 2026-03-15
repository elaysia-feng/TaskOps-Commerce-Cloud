package com.elias.wallet.service.impl;

import com.elias.common.mq.event.TaskSettlementRequestedEvent;
import com.elias.wallet.service.WalletSettlementService;
import org.springframework.stereotype.Service;

@Service
public class WalletSettlementServiceImpl implements WalletSettlementService {

    @Override
    public void handleTaskSettlementRequested(TaskSettlementRequestedEvent event) {
        throw new UnsupportedOperationException("TODO: implement task settlement handling");
    }
}