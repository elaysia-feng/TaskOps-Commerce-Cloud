package com.elias.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elias.task.entity.Wallet;
import com.elias.task.entity.WalletFlow;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WalletFlowMapper extends BaseMapper<WalletFlow> {
}
