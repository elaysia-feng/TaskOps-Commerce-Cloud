package com.elias.order.mq.consumer;

import com.elias.common.mq.MqConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayFailConsumer {

    @RabbitListener(queues = MqConstants.QUEUE_PAY_FAIL_ORDER_UPDATE)
    public void onPayFail(Object event) {
        if (event == null) {
            log.warn("ignore invalid pay fail event");
            return;
        }
        // TODO 这里处理支付失败后的订单关闭、状态回滚或补偿逻辑。
    }
}