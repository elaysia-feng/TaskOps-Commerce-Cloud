package com.elias.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elias.pay.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}
