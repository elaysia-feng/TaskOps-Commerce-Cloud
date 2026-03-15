package com.elias.wallet.service.impl;

import com.elias.wallet.service.WalletAccountService;
import com.elias.wallet.vo.WalletOverviewVO;
import org.springframework.stereotype.Service;

@Service
public class WalletAccountServiceImpl implements WalletAccountService {

    @Override
    public WalletOverviewVO queryOverview(Long userId) {
        throw new UnsupportedOperationException("TODO: implement wallet overview query");
    }
}