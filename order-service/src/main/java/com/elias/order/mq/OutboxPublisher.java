package com.elias.order.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elias.common.mq.MqConstants;
import com.elias.order.entity.OrderOutbox;
import com.elias.order.mapper.OrderOutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OrderOutboxMapper outboxMapper;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void publishPendingMessages() {
        List<OrderOutbox> list = outboxMapper.selectList(new LambdaQueryWrapper<OrderOutbox>()
                .eq(OrderOutbox::getStatus, 0)
                .le(OrderOutbox::getNextRetryTime, LocalDateTime.now())
                .orderByAsc(OrderOutbox::getId)
                .last("limit 50"));

        for (OrderOutbox outbox : list) {
            try {
                rabbitTemplate.convertAndSend(MqConstants.EXCHANGE_BUSINESS, outbox.getRoutingKey(), outbox.getPayload(), message -> {
                    message.getMessageProperties().setMessageId(outbox.getMessageId());
                    message.getMessageProperties().setContentType("application/json");
                    return message;
                });
                outbox.setStatus(1);
                outbox.setUpdatedAt(LocalDateTime.now());
                outboxMapper.updateById(outbox);
            } catch (Exception e) {
                int retry = outbox.getRetryCount() + 1;
                outbox.setRetryCount(retry);
                outbox.setNextRetryTime(LocalDateTime.now().plusSeconds(Math.min(60, retry * 5L)));
                outbox.setUpdatedAt(LocalDateTime.now());
                outboxMapper.updateById(outbox);
                log.warn("publish outbox failed, msgId={}", outbox.getMessageId(), e);
            }
        }
    }
}
