package com.elias.pay.controller;

import com.elias.common.ApiResponse;
import com.elias.common.exception.ErrorCode;
import com.elias.pay.dto.PayCallbackRequest;
import com.elias.pay.entity.Payment;
import com.elias.pay.service.PaymentService;
import com.elias.pay.vo.PayCreateVO;
import com.elias.pay.vo.PayDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
@Validated
public class PayController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    //创建支付订单
    public ApiResponse<PayCreateVO> createPayOrder(@RequestParam String orderNo){
        return ApiResponse.ok(paymentService.createPayOrder(orderNo));
    }

    @GetMapping("/internal/summary")
    public ApiResponse<Map<String, Long>> summary() {
        return ApiResponse.ok(paymentService.summary());
    }

    //查询订单详情
    @GetMapping("/{orderNo}")
    public ApiResponse<PayDetailVO> getPaymentDetail(@PathVariable @NotBlank String orderNo) {
        return ApiResponse.ok(paymentService.getPaymentDetail(orderNo));
    }

    //关闭支付订单
    @PostMapping("/{orderNo}/close")
    public ApiResponse<Void> closePayOrder(@PathVariable @NotBlank String orderNo) {
        paymentService.closePayOrder(orderNo);
        return ApiResponse.ok();
    }
}
