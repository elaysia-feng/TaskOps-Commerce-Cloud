package com.elias.wallet.service.impl;

import com.elias.wallet.dto.WithdrawApplyRequest;
import com.elias.wallet.dto.WithdrawAuditRequest;
import com.elias.wallet.service.WithdrawService;
import com.elias.wallet.vo.PageResponse;
import com.elias.wallet.vo.WithdrawRecordVO;
import org.springframework.stereotype.Service;

@Service
public class WithdrawServiceImpl implements WithdrawService {

    @Override
    public WithdrawRecordVO apply(Long userId, WithdrawApplyRequest request) {
        throw new UnsupportedOperationException("TODO: implement withdraw apply");
    }

    @Override
    public PageResponse<WithdrawRecordVO> pageUserWithdraws(Long userId, long pageNum, long pageSize) {
        throw new UnsupportedOperationException("TODO: implement user withdraw page query");
    }

    @Override
    public PageResponse<WithdrawRecordVO> pageAllWithdraws(long pageNum, long pageSize) {
        throw new UnsupportedOperationException("TODO: implement admin withdraw page query");
    }

    @Override
    public void approve(Long withdrawId, Long auditBy) {
        throw new UnsupportedOperationException("TODO: implement withdraw approval");
    }

    @Override
    public void reject(Long withdrawId, Long auditBy, WithdrawAuditRequest request) {
        throw new UnsupportedOperationException("TODO: implement withdraw rejection");
    }
}