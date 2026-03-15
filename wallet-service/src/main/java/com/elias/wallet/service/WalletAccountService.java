package com.elias.wallet.service;

import com.elias.wallet.vo.WalletOverviewVO;

public interface WalletAccountService {

    WalletOverviewVO queryOverview(Long userId);
}