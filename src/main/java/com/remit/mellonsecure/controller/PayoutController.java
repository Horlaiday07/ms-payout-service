package com.remit.mellonsecure.controller;

import com.remit.mellonsecure.dto.PayoutResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payout")
public class PayoutController {

    private static final Logger log = LoggerFactory.getLogger(PayoutController.class);

    @PostMapping
    public ResponseEntity<PayoutResponse> createPayout(
            @RequestHeader(value = "X-Merchant-Id", required = true) String merchantId,
            HttpServletRequest request) {

        String correlationId = request.getHeader("X-Correlation-Id");
        log.info("Payout request received: merchantId={}, correlationId={}", mask(merchantId), correlationId);

        PayoutResponse response = new PayoutResponse("success", merchantId);
        return ResponseEntity.ok(response);
    }

    private String mask(String value) {
        if (value == null || value.isEmpty()) return "-";
        if (value.length() <= 4) return "****";
        return value.substring(0, 2) + "***";
    }
}
