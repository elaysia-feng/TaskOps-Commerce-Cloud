package com.elias.wallet.mq.consumer;

import com.elias.common.mq.MqConstants;
import com.elias.common.mq.event.TaskSettlementRequestedEvent;
import com.elias.wallet.service.WalletSettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskSettlementRequestedConsumer {

    private final WalletSettlementService walletSettlementService;

    @RabbitListener(queues = MqConstants.QUEUE_TASK_SETTLEMENT_REQUESTED_WALLET_SETTLE)
    public void onTaskSettlementRequested(TaskSettlementRequestedEvent event) {
        walletSettlementService.handleTaskSettlementRequested(event);
    }
}