package com.application.presentation.payment.controller;

import static com.response.ResponseUtil.*;
import static com.vo.UserRole.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.presentation.payment.dto.request.TossCancelRequest;
import com.application.presentation.payment.dto.request.TossConfirmRequest;
import com.application.presentation.payment.dto.request.TossWebhookRequest;
import com.application.presentation.payment.dto.response.PaymentResponse;
import com.application.presentation.payment.dto.response.TossPaymentQueryResponse;
import com.authorization.AssignUserPassport;
import com.authorization.HasRole;
import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.response.ResponseBody;
import com.vo.UserPassport;

import domain.pos.payment.entity.Payment;
import domain.pos.payment.entity.TossConfirmResult;
import domain.pos.payment.port.required.TossPaymentPort;
import domain.pos.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final TossPaymentPort tossPaymentPort;
    private final ObjectMapper objectMapper;

    @PostMapping("/api/v1/payments/toss/confirm")
    public ResponseEntity<ResponseBody<PaymentResponse>> confirmPayment(
        @RequestBody @Valid TossConfirmRequest request) {
        Payment payment = paymentService.confirmPayment(
            request.paymentKey(), request.orderId(), request.amount());
        return ResponseEntity.ok(createSuccessResponse(PaymentResponse.from(payment)));
    }

    @PostMapping("/api/v1/payments/{paymentKey}/cancel")
    @HasRole(userRole = ROLE_OWNER)
    @AssignUserPassport
    public ResponseEntity<ResponseBody<Void>> cancelPayment(
        UserPassport userPassport,
        @PathVariable String paymentKey,
        @RequestBody @Valid TossCancelRequest request) {
        paymentService.cancelPayment(paymentKey, request.cancelReason(), request.cancelAmount(), userPassport);
        return ResponseEntity.ok(createSuccessResponse());
    }

    @GetMapping("/api/v1/payments")
    @HasRole(userRole = ROLE_OWNER)
    public ResponseEntity<ResponseBody<List<PaymentResponse>>> getPaymentsBySale(
        @RequestParam Long saleId) {
        List<PaymentResponse> responses = paymentService.findPaymentsBySaleId(saleId).stream()
            .map(PaymentResponse::from)
            .toList();
        return ResponseEntity.ok(createSuccessResponse(responses));
    }

    @GetMapping("/api/v1/payments/receipts/{receiptId}")
    public ResponseEntity<ResponseBody<PaymentResponse>> getPaymentByReceipt(
        @PathVariable UUID receiptId) {
        return paymentService.findPaymentByReceiptId(receiptId)
            .map(payment -> ResponseEntity.ok(createSuccessResponse(PaymentResponse.from(payment))))
            .orElseGet(() -> ResponseEntity.ok(createSuccessResponse(null)));
    }

    @GetMapping("/api/v1/payments/toss/{paymentKey}")
    @HasRole(userRole = ROLE_OWNER)
    public ResponseEntity<ResponseBody<TossPaymentQueryResponse>> getTossPayment(
        @PathVariable String paymentKey) {
        TossConfirmResult result = paymentService.getPaymentFromToss(paymentKey);
        return ResponseEntity.ok(createSuccessResponse(TossPaymentQueryResponse.from(result)));
    }

    @PostMapping("/api/v1/payments/toss/webhook")
    public ResponseEntity<Void> handleTossWebhook(
        @RequestHeader(value = "TossPayments-Signature", required = false) String signature,
        @RequestBody String rawBody) {
        tossPaymentPort.verifyWebhookSignature(rawBody, signature);

        try {
            TossWebhookRequest request = objectMapper.readValue(rawBody, TossWebhookRequest.class);
            if ("PAYMENT_STATUS_CHANGED".equals(request.eventType()) && request.data() != null) {
                paymentService.processWebhook(request.data().paymentKey(), request.data().status());
            }
        } catch (IOException e) {
            log.warn("웹훅 요청 본문 파싱 실패. body={}", rawBody);
            throw new ServiceException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return ResponseEntity.ok().build();
    }
}
