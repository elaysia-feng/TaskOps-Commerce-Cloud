package com.elias.order.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elias.common.mq.MqConstants;
import com.elias.common.mq.event.PaySuccessEvent;
import com.elias.order.entity.MessageConsumeLog;
import com.elias.order.mapper.MessageConsumeLogMapper;
import com.elias.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaySuccessConsumer {

    private final MessageConsumeLogMapper consumeLogMapper;
    private final OrderService orderService;

    @RabbitListener(queues = MqConstants.QUEUE_ORDER_PAY_SUCCESS)
    @Transactional
    public void onMessage(PaySuccessEvent event) {
        if (event == null || event.getMessageId() == null) {
            return;
        }

        MessageConsumeLog existed = consumeLogMapper.selectOne(new LambdaQueryWrapper<MessageConsumeLog>()
                .eq(MessageConsumeLog::getMessageId, event.getMessageId())
                .eq(MessageConsumeLog::getBizType, "PAY_SUCCESS"));
        if (existed != null) {
            return;
        }

        orderService.markPaid(event.getOrderNo());

        MessageConsumeLog logRow = new MessageConsumeLog();
        logRow.setMessageId(event.getMessageId());
        logRow.setBizType("PAY_SUCCESS");
        logRow.setCreatedAt(LocalDateTime.now());
        consumeLogMapper.insert(logRow);
        log.info("consume pay success, orderNo={}", event.getOrderNo());
    }
}
