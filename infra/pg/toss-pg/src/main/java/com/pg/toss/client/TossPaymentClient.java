package com.pg.toss.client;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.payment.entity.PaymentStatus;
import domain.pos.payment.entity.TossConfirmResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    private static final String CONFIRM_PATH = "/v1/payments/confirm";
    private static final String CANCEL_PATH = "/v1/payments/{paymentKey}/cancel";

    private final RestClient tossRestClient;

    public TossConfirmResult confirm(String paymentKey, String orderId, Integer amount) {
        Map<String, Object> body = Map.of(
            "paymentKey", paymentKey,
            "orderId", orderId,
            "amount", amount
        );

        TossPaymentResponse response = tossRestClient.post()
            .uri(CONFIRM_PATH)
            .body(body)
            .retrieve()
            .onStatus(HttpStatusCode::isError, (req, res) -> {
                String errorBody = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                log.warn("토스페이먼츠 결제 승인 실패. status={}, body={}", res.getStatusCode(), errorBody);
                throw new ServiceException(ErrorCode.PAYMENT_CONFIRM_FAILED);
            })
            .body(TossPaymentResponse.class);

        if (response == null) {
            throw new ServiceException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        return TossConfirmResult.builder()
            .tossPaymentKey(response.paymentKey())
            .tossOrderId(response.orderId())
            .amount(response.totalAmount())
            .status(PaymentStatus.DONE)
            .paymentMethod(response.method())
            .approvedAt(response.approvedAt() != null ? response.approvedAt().toLocalDateTime() : null)
            .build();
    }

    public PaymentStatus cancel(String paymentKey, String cancelReason, Integer cancelAmount) {
        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", cancelReason);
        if (cancelAmount != null) {
            body.put("cancelAmount", cancelAmount);
        }

        TossCancelResponse response = tossRestClient.post()
            .uri(CANCEL_PATH, paymentKey)
            .body(body)
            .retrieve()
            .onStatus(HttpStatusCode::isError, (req, res) -> {
                String errorBody = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                log.warn("토스페이먼츠 결제 취소 실패. paymentKey={}, status={}, body={}",
                    paymentKey, res.getStatusCode(), errorBody);
                throw new ServiceException(ErrorCode.PAYMENT_CANCEL_FAILED);
            })
            .body(TossCancelResponse.class);

        if (response == null) {
            throw new ServiceException(ErrorCode.PAYMENT_CANCEL_FAILED);
        }

        log.info("토스페이먼츠 결제 취소 완료. paymentKey={}, resultStatus={}", paymentKey, response.status());

        try {
            return PaymentStatus.valueOf(response.status());
        } catch (IllegalArgumentException e) {
            log.warn("토스페이먼츠 취소 응답 상태값 파싱 실패. status={}", response.status());
            return PaymentStatus.CANCELED;
        }
    }

    private record TossPaymentResponse(
        String paymentKey,
        String orderId,
        Integer totalAmount,
        String method,
        String status,
        OffsetDateTime approvedAt
    ) {
    }

    private record TossCancelResponse(String status) {
    }
}
