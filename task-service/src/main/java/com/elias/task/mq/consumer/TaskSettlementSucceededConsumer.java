package com.elias.task.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elias.common.mq.MqConstants;
import com.elias.common.mq.event.TaskSettlementSucceededEvent;
import com.elias.task.entity.InternshipTask;
import com.elias.task.entity.TaskSettlement;
import com.elias.task.mapper.InternshipTaskMapper;
import com.elias.task.mapper.TaskSettlementMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskSettlementSucceededConsumer {

    private final TaskSettlementMapper taskSettlementMapper;
    private final InternshipTaskMapper taskMapper;

    @Transactional
    @RabbitListener(queues = MqConstants.QUEUE_WALLET_SETTLEMENT_SUCCEEDED_TASK_UPDATE)
    public void onTaskSettlementSucceeded(TaskSettlementSucceededEvent event) {
        if (event == null || event.getTaskId() == null || event.getSettlementId() == null) {
            log.warn("ignore invalid task settlement success event: {}", event);
            return;
        }

        TaskSettlement settlement = taskSettlementMapper.selectOne(
                new LambdaQueryWrapper<TaskSettlement>()
                        .eq(TaskSettlement::getId, event.getSettlementId())
                        .eq(TaskSettlement::getTaskId, event.getTaskId())
                        .last("limit 1")
        );
        if (settlement == null) {
            log.warn("task settlement not found, settlementId={}, taskId={}",
                    event.getSettlementId(), event.getTaskId());
            return;
        }

        if ("SUCCESS".equals(settlement.getStatus())) {
            log.info("ignore repeated success event, settlementId={}", settlement.getId());
            return;
        }

        LocalDateTime now = event.getSettledAt() == null ? LocalDateTime.now() : event.getSettledAt();

        settlement.setStatus("SUCCESS");
        settlement.setSettledAt(now);
        settlement.setUpdatedAt(now);
        taskSettlementMapper.updateById(settlement);

        InternshipTask task = taskMapper.selectById(event.getTaskId());
        if (task != null && !"SETTLED".equals(task.getStatus())) {
            task.setStatus("SETTLED");
            task.setCompletedAt(now);
            task.setUpdatedAt(now);
            taskMapper.updateById(task);
        }

        log.info("task settlement succeeded, settlementId={}, taskId={}",
                settlement.getId(), event.getTaskId());
    }

}