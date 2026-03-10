package com.elias.task.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elias.common.mq.MqConstants;
import com.elias.common.mq.event.TaskSettlementFailedEvent;
import com.elias.task.entity.TaskSettlement;
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
public class TaskSettlementFailedConsumer {
    private final TaskSettlementMapper taskSettlementMapper;

    @Transactional
    @RabbitListener(queues = MqConstants.QUEUE_WALLET_SETTLEMENT_FAILED_TASK_UPDATE)
    public void onTaskSettlementFailed(TaskSettlementFailedEvent event) {
        // 1. 基本参数校验，避免处理空消息或关键字段缺失的消息
        if (event == null || event.getTaskId() == null || event.getSettlementId() == null) {
            log.warn("ignore invalid task settlement failed event: {}", event);
            return;
        }

        // 2. 根据结算ID和任务ID查询结算记录
        TaskSettlement settlement = taskSettlementMapper.selectOne(
                new LambdaQueryWrapper<TaskSettlement>()
                        .eq(TaskSettlement::getId, event.getSettlementId())
                        .eq(TaskSettlement::getTaskId, event.getTaskId())
                        .last("limit 1")
        );

        // 3. 结算记录不存在时，直接忽略
        if (settlement == null) {
            log.warn("task settlement not found, settlementId={}, taskId={}", event.getSettlementId(), event.getTaskId());
            return;
        }

        // 4. 如果已经成功，就忽略失败消息，防止状态被回退
        if ("SUCCESS".equals(settlement.getStatus())) {
            log.info("ignore failed event because settlement already succeeded, settlementId={}", settlement.getId());
            return;
        }

        // 5. 如果已经是失败状态，说明是重复消息，直接忽略
        if ("FAILED".equals(settlement.getStatus())) {
            log.info("ignore repeated failed event, settlementId={}", settlement.getId());
            return;
        }

        // 6. 更新结算状态为失败，并记录失败原因
        settlement.setStatus("FAILED");
        settlement.setFailReason(event.getFailReason());
        settlement.setUpdatedAt(LocalDateTime.now());
        taskSettlementMapper.updateById(settlement);

        // 7. 记录日志，后续如有需要可在这里扩展补偿逻辑
        log.warn("task settlement marked failed, settlementId={}, taskId={}, reason={}",
                settlement.getId(), settlement.getTaskId(), event.getFailReason());

        // TODO 如需自动补偿，可在这里新增重试记录或投递补偿任务。
    }
}