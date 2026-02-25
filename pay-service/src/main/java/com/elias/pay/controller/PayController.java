package com.elias.pay.controller;

import com.elias.common.ApiResponse;
import com.elias.pay.dto.PayCallbackRequest;
import com.elias.pay.entity.Payment;
import com.elias.pay.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
@Validated
public class PayController {

    private final PaymentService paymentService;

    @PostMapping("/callback/mock")
    public ApiResponse<Payment> mockCallback(@Valid @RequestBody PayCallbackRequest request) {
        return ApiResponse.ok(paymentService.mockPaySuccess(request.getOrderNo()));
    }

    @GetMapping("/internal/summary")
    public ApiResponse<Map<String, Long>> summary() {
        return ApiResponse.ok(paymentService.summary());
    }
}
