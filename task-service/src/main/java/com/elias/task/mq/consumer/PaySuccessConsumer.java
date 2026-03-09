package com.elias.task.mq.consumer;

import com.elias.common.mq.MqConstants;
import com.elias.common.mq.event.PaySuccessEvent;
import com.elias.task.membership.MembershipLevel;
import com.elias.task.membership.TaskQuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaySuccessConsumer {

    private static final String SKU_MEMBER_VIP = "MEMBER_VIP";
    private static final String SKU_MEMBER_SVIP = "MEMBER_SVIP";

    private final TaskQuotaService taskQuotaService;

    @RabbitListener(queues = MqConstants.QUEUE_PAY_SUCCESS_TASK)
    public void listenTaskQueue(PaySuccessEvent event) {
        if (event == null || event.getOrderNo() == null || event.getUserId() == null) {
            log.warn("ignore invalid pay success event: {}", event);
            return;
        }
        MembershipLevel level = resolveLevel(event.getSkuCode());
        if (level != null) {
            taskQuotaService.setLevel(event.getUserId(), level);
            log.info("task-service upgrade membership success, userId={}, level={}, orderNo={}",
                    event.getUserId(), level.name(), event.getOrderNo());
        } else {
            log.info("task-service consume pay.success, no membership change, skuCode={}, orderNo={}",
                    event.getSkuCode(), event.getOrderNo());
        }
    }

    private MembershipLevel resolveLevel(String skuCode) {
        if (SKU_MEMBER_SVIP.equals(skuCode)) {
            return MembershipLevel.SVIP;
        }
        if (SKU_MEMBER_VIP.equals(skuCode)) {
            return MembershipLevel.VIP;
        }
        return null;
    }
}
