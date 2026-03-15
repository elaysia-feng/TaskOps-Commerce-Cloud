package com.elias.wallet.service;

import com.elias.common.mq.event.TaskSettlementRequestedEvent;

public interface WalletSettlementService {

    void handleTaskSettlementRequested(TaskSettlementRequestedEvent event);
}