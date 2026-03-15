package com.elias.wallet.mq.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WalletEventProducer {

    public void sendTaskSettlementSucceeded() {
        throw new UnsupportedOperationException("TODO: implement settlement success event publish");
    }

    public void sendTaskSettlementFailed() {
        throw new UnsupportedOperationException("TODO: implement settlement failed event publish");
    }
}