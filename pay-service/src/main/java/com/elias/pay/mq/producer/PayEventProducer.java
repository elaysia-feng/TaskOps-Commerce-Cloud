package com.elias.pay.mq.producer;

import com.elias.common.mq.MqConstants;
import com.elias.common.mq.event.PaySuccessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendPaySuccess(PaySuccessEvent event) {
        rabbitTemplate.convertAndSend(
                MqConstants.EXCHANGE_BUSINESS,
                MqConstants.RK_PAY_SUCCESS,
                event
        );
    }
}
