package com.elias.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elias.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("""
            SELECT o.id,
                   o.order_no AS orderNo,
                   o.buyer_user_id AS userId,
                   o.biz_type AS bizType,
                   o.biz_no AS bizNo,
                   o.order_title AS orderTitle,
                   o.order_desc AS orderDesc,
                   o.currency_code AS currencyCode,
                   o.total_amount AS totalAmount,
                   o.discount_amount AS discountAmount,
                   o.payable_amount AS amount,
                   o.paid_amount AS paidAmount,
                   o.status,
                   o.close_reason AS closeReason,
                   o.expired_at AS expiredAt,
                   o.paid_at AS paidAt,
                   o.closed_at AS closedAt,
                   o.created_at AS createdAt,
                   o.updated_at AS updatedAt,
                   o.version,
                   i.item_code AS skuCode,
                   i.quantity AS quantity
            FROM trade_order o
            LEFT JOIN trade_order_item i ON i.order_id = o.id
            WHERE o.order_no = #{orderNo}
            LIMIT 1
            """)
    Order selectDetailByOrderNo(String orderNo);

    @Select("""
            SELECT o.id,
                   o.order_no AS orderNo,
                   o.buyer_user_id AS userId,
                   o.biz_type AS bizType,
                   o.biz_no AS bizNo,
                   o.order_title AS orderTitle,
                   o.order_desc AS orderDesc,
                   o.currency_code AS currencyCode,
                   o.total_amount AS totalAmount,
                   o.discount_amount AS discountAmount,
                   o.payable_amount AS amount,
                   o.paid_amount AS paidAmount,
                   o.status,
                   o.close_reason AS closeReason,
                   o.expired_at AS expiredAt,
                   o.paid_at AS paidAt,
                   o.closed_at AS closedAt,
                   o.created_at AS createdAt,
                   o.updated_at AS updatedAt,
                   o.version,
                   i.item_code AS skuCode,
                   i.quantity AS quantity
            FROM trade_order o
            LEFT JOIN trade_order_item i ON i.order_id = o.id
            WHERE o.buyer_user_id = #{userId}
            ORDER BY o.created_at DESC
            """)
    List<Order> selectDetailsByUserId(Long userId);
}