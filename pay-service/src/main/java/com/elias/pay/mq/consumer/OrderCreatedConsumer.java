package com.elias.pay.mq.consumer;

import com.elias.common.mq.MqConstants;
import com.elias.common.mq.event.OrderCreatedEvent;
import com.elias.pay.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final PaymentService paymentService;

    @RabbitListener(queues = MqConstants.QUEUE_ORDER_CREATED_PAY)
    public void onOrderCreated(OrderCreatedEvent event) {
        if (event == null || event.getOrderNo() == null || event.getAmount() == null) {
            log.warn("ignore invalid order created event: {}", event);
            return;
        }
        paymentService.createPaymentIfAbsent(event.getMessageId(), event.getOrderNo(), event.getAmount());
        log.info("consume order.created, payment prepared, orderNo={}", event.getOrderNo());
    }
}
