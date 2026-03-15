package com.elias.wallet.controller;

import com.elias.common.ApiResponse;
import com.elias.wallet.dto.WithdrawApplyRequest;
import com.elias.wallet.service.WalletAccountService;
import com.elias.wallet.service.WalletFlowService;
import com.elias.wallet.service.WithdrawService;
import com.elias.wallet.vo.PageResponse;
import com.elias.wallet.vo.WalletFlowVO;
import com.elias.wallet.vo.WalletOverviewVO;
import com.elias.wallet.vo.WithdrawRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletAccountService walletAccountService;
    private final WalletFlowService walletFlowService;
    private final WithdrawService withdrawService;

    @GetMapping("/me")
    public ApiResponse<WalletOverviewVO> me(@RequestParam Long userId) {
        return ApiResponse.ok(walletAccountService.queryOverview(userId));
    }

    @GetMapping("/flows")
    public ApiResponse<PageResponse<WalletFlowVO>> flows(@RequestParam Long userId,
                                                         @RequestParam(defaultValue = "1") long pageNum,
                                                         @RequestParam(defaultValue = "10") long pageSize,
                                                         @RequestParam(required = false) String flowType) {
        return ApiResponse.ok(walletFlowService.pageUserFlows(userId, flowType, pageNum, pageSize));
    }

    @PostMapping("/withdraw")
    public ApiResponse<WithdrawRecordVO> applyWithdraw(@RequestParam Long userId,
                                                       @Valid @RequestBody WithdrawApplyRequest request) {
        return ApiResponse.ok(withdrawService.apply(userId, request));
    }

    @GetMapping("/withdraws")
    public ApiResponse<PageResponse<WithdrawRecordVO>> withdraws(@RequestParam Long userId,
                                                                 @RequestParam(defaultValue = "1") long pageNum,
                                                                 @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(withdrawService.pageUserWithdraws(userId, pageNum, pageSize));
    }
}