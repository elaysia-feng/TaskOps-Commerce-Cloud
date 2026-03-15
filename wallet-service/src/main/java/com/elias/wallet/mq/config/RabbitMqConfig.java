package com.elias.wallet.mq.config;

import com.elias.common.mq.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public DirectExchange businessExchange() {
        return new DirectExchange(MqConstants.EXCHANGE_BUSINESS, true, false);
    }

    @Bean
    public Queue taskSettlementRequestedWalletSettleQueue() {
        return new Queue(MqConstants.QUEUE_TASK_SETTLEMENT_REQUESTED_WALLET_SETTLE, true);
    }

    @Bean
    public Binding taskSettlementRequestedBinding(
            @Qualifier("taskSettlementRequestedWalletSettleQueue") Queue taskSettlementRequestedWalletSettleQueue,
            DirectExchange businessExchange) {
        return BindingBuilder.bind(taskSettlementRequestedWalletSettleQueue)
                .to(businessExchange)
                .with(MqConstants.RK_TASK_SETTLEMENT_REQUESTED);
    }
}