package com.elias.wallet.service;

import com.elias.wallet.dto.WithdrawApplyRequest;
import com.elias.wallet.dto.WithdrawAuditRequest;
import com.elias.wallet.vo.PageResponse;
import com.elias.wallet.vo.WithdrawRecordVO;

public interface WithdrawService {

    WithdrawRecordVO apply(Long userId, WithdrawApplyRequest request);

    PageResponse<WithdrawRecordVO> pageUserWithdraws(Long userId, long pageNum, long pageSize);

    PageResponse<WithdrawRecordVO> pageAllWithdraws(long pageNum, long pageSize);

    void approve(Long withdrawId, Long auditBy);

    void reject(Long withdrawId, Long auditBy, WithdrawAuditRequest request);
}