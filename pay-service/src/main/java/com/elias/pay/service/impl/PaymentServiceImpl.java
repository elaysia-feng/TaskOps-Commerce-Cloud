package com.elias.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elias.common.exception.BizException;
import com.elias.pay.client.OrderClient;
import com.elias.pay.entity.MessageConsumeLog;
import com.elias.pay.entity.Payment;
import com.elias.pay.mapper.MessageConsumeLogMapper;
import com.elias.pay.mapper.PaymentMapper;
import com.elias.pay.service.PaymentService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
//TODO改成真实的扣款,并增加一个库存和个人余额的微服务
public class PaymentServiceImpl implements PaymentService {

    // 支付表 Mapper：负责 Payment 的 CRUD
    private final PaymentMapper paymentMapper;

    // 消费日志表 Mapper：用于记录消息是否已处理，做幂等
    private final MessageConsumeLogMapper consumeLogMapper;

    // 远程调用订单服务：用于标记订单已支付
    private final OrderClient orderClient;

    /**
     * 如果不存在支付记录则创建支付记录，并写入消息消费日志（幂等）。
     *
     * 业务含义：
     * - messageId + bizType(ORDER_CREATED) 在 consume_log 表中唯一（或应当唯一）
     * - 同一条消息重复投递时，通过消费日志直接 return，避免重复创建支付单
     *
     * 事务含义：
     * - 支付记录插入 + 消费日志插入在同一事务中提交/回滚，保证一致性
     */
    @Override
    @GlobalTransactional
    public void createPaymentIfAbsent(String messageId, String orderNo, BigDecimal amount) {

        // 1) 先查消费日志：如果该 messageId 已处理过（ORDER_CREATED），直接返回（幂等）
        MessageConsumeLog existedLog = consumeLogMapper.selectOne(new LambdaQueryWrapper<MessageConsumeLog>()
                .eq(MessageConsumeLog::getMessageId, messageId)
                .eq(MessageConsumeLog::getBizType, "ORDER_CREATED"));
        if (existedLog != null) {
            return;
        }

        // 2) 再查支付记录：同一订单只允许存在一笔支付单（或至少避免重复插入）
        Payment existedPay = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>().eq(Payment::getOrderNo, orderNo)
        );

        // 3) 若支付记录不存在则创建，初始状态为 PENDING
        if (existedPay == null) {
            Payment payment = new Payment();
            // 生成支付单号：PAY + 时间戳 + 随机串（示例用法，生产可用更稳定的生成策略）
            payment.setPayNo("PAY" + System.currentTimeMillis()
                    + UUID.randomUUID().toString().substring(0, 6).toUpperCase());

            payment.setOrderNo(orderNo);
            payment.setAmount(amount);
            payment.setStatus("PENDING");
            payment.setCreatedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentMapper.insert(payment);
        }

        // 4) 写入消息消费日志：标记该 ORDER_CREATED 消息已被处理（幂等落库）
        MessageConsumeLog row = new MessageConsumeLog();
        row.setMessageId(messageId);
        row.setBizType("ORDER_CREATED");
        row.setCreatedAt(LocalDateTime.now());
        consumeLogMapper.insert(row);

        // 注意：当前实现存在并发窗口
        // - 多线程/多实例同时处理同一 messageId 时，可能同时查到 existedLog==null，然后都插入
        // - 推荐在数据库上加唯一约束： (message_id, biz_type) 或 message_id
        //   并在插入时捕获 DuplicateKeyException 来保证幂等的强一致
    }

    /**
     * 模拟支付成功：
     * - 将本地支付单状态更新为 SUCCESS
     * - 调用订单服务将订单标记为已支付
     *
     * 事务注意：
     * - 当前方法在一个本地事务中，但 orderClient.markPaid 是远程调用，不受本地事务控制
     * - 可能出现：本地已提交 SUCCESS，但远程调用失败（或反过来）
     * - 生产中通常用 Outbox/消息最终一致性补偿来处理
     */
    @Override
    @GlobalTransactional
    public Payment mockPaySuccess(String orderNo) {

        // 1) 查支付单，不存在则抛业务异常
        Payment payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>().eq(Payment::getOrderNo, orderNo)
        );
        if (payment == null) {
            throw new BizException(4043, "payment not found");
        }

        // 2) 若未成功则更新为 SUCCESS（避免重复更新）
        if (!"SUCCESS".equals(payment.getStatus())) {
            payment.setStatus("SUCCESS");
            payment.setUpdatedAt(LocalDateTime.now());
            paymentMapper.updateById(payment);
        }

        // 3) 通知订单服务：将订单标记为已支付
        // 注意：远程调用失败会抛异常，导致本地事务回滚（如果异常未被吞）
        // 但即使本地回滚了，远程服务可能已执行成功（取决于远程实现与网络情况）
        orderClient.markPaid(payment.getOrderNo());

        return payment;
    }

    /**
     * 汇总支付数据：
     * - total：总支付单数
     * - pending：待支付数
     * - success：已支付数
     *
     * 说明：这里做了 3 次 count 查询，数据量大时可考虑一次 SQL 聚合统计减少 DB 交互。
     */
    @Override
    public Map<String, Long> summary() {
        Map<String, Long> result = new HashMap<>();

        // 总数
        result.put("total", paymentMapper.selectCount(new LambdaQueryWrapper<>()));

        // 待支付数
        result.put("pending", paymentMapper.selectCount(
                new LambdaQueryWrapper<Payment>().eq(Payment::getStatus, "PENDING")
        ));

        // 已支付数
        result.put("success", paymentMapper.selectCount(
                new LambdaQueryWrapper<Payment>().eq(Payment::getStatus, "SUCCESS")
        ));

        return result;
    }
}