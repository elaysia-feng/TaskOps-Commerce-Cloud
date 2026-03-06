package com.elias.order.mq.consumer;

import com.elias.common.mq.MqConstants;
import com.elias.common.mq.event.PaySuccessEvent;
import com.elias.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DirectListener {

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.QUEUE_ORDER_PAY_SUCCESS, durable = "true"),
            exchange = @Exchange(name = MqConstants.EXCHANGE_BUSINESS, type = ExchangeTypes.DIRECT, durable = "true"),
            key = MqConstants.RK_PAY_SUCCESS
    ))
    public void listenDirectQueue(PaySuccessEvent event) {
        if (event == null || event.getOrderNo() == null) {
            log.warn("ignore invalid pay success event: {}", event);
            return;
        }
        orderService.markPaid(event.getOrderNo());
        log.info("consume pay.success, order marked paid, orderNo={}", event.getOrderNo());
    }

    private final OrderService orderService;
}
