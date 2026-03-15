package com.elias.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elias.wallet.entity.Wallet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WalletMapper extends BaseMapper<Wallet> {
}