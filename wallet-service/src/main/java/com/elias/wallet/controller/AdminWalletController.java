package com.elias.wallet.controller;

import com.elias.common.ApiResponse;
import com.elias.wallet.dto.WithdrawAuditRequest;
import com.elias.wallet.service.WalletAccountService;
import com.elias.wallet.service.WithdrawService;
import com.elias.wallet.vo.PageResponse;
import com.elias.wallet.vo.WalletOverviewVO;
import com.elias.wallet.vo.WithdrawRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/wallet")
@RequiredArgsConstructor
public class AdminWalletController {

    private final WalletAccountService walletAccountService;
    private final WithdrawService withdrawService;

    @GetMapping("/user/{userId}")
    public ApiResponse<WalletOverviewVO> userWallet(@PathVariable Long userId) {
        return ApiResponse.ok(walletAccountService.queryOverview(userId));
    }

    @GetMapping("/withdraws")
    public ApiResponse<PageResponse<WithdrawRecordVO>> withdraws(@RequestParam(defaultValue = "1") long pageNum,
                                                                 @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(withdrawService.pageAllWithdraws(pageNum, pageSize));
    }

    @PostMapping("/withdraw/{withdrawId}/approve")
    public ApiResponse<Void> approve(@PathVariable Long withdrawId,
                                     @RequestParam(required = false) Long auditBy) {
        withdrawService.approve(withdrawId, auditBy);
        return ApiResponse.ok();
    }

    @PostMapping("/withdraw/{withdrawId}/reject")
    public ApiResponse<Void> reject(@PathVariable Long withdrawId,
                                    @RequestParam(required = false) Long auditBy,
                                    @RequestBody(required = false) WithdrawAuditRequest request) {
        withdrawService.reject(withdrawId, auditBy, request);
        return ApiResponse.ok();
    }
}