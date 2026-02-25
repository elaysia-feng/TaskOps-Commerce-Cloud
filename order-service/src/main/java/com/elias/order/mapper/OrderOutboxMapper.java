package com.elias.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elias.order.entity.OrderOutbox;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderOutboxMapper extends BaseMapper<OrderOutbox> {
}
