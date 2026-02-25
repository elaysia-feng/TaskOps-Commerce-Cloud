package com.elias.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elias.order.entity.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {

    @Update("update t_stock set stock = stock - #{qty}, updated_at = now() where sku_code = #{skuCode} and stock >= #{qty}")
    int deductStock(@Param("skuCode") String skuCode, @Param("qty") Integer qty);

    @Update("update t_stock set stock = stock + #{qty}, updated_at = now() where sku_code = #{skuCode}")
    int restoreStock(@Param("skuCode") String skuCode, @Param("qty") Integer qty);
}
