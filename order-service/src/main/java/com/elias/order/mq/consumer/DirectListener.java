package com.elias.order.mq.consumer;

import com.elias.common.mq.MqConstants;
import com.elias.common.mq.event.PaySuccessEvent;
import com.elias.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DirectListener {

    private final OrderService orderService;

    @RabbitListener(queues = MqConstants.QUEUE_PAY_SUCCESS_ORDER_UPDATE)
    public void listenPaySuccess(PaySuccessEvent event) {
        if (event == null || event.getOrderNo() == null) {
            log.warn("ignore invalid pay success event: {}", event);
            return;
        }
        orderService.markPaid(event.getOrderNo());
        log.info("consume pay.success, order marked paid, orderNo={}", event.getOrderNo());
    }
}