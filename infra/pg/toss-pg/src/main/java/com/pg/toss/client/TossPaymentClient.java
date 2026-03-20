package com.pg.toss.client;

import java.time.LocalDateTime;
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
                log.warn("토스페이먼츠 결제 승인 실패. status={}", res.getStatusCode());
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
            .approvedAt(response.approvedAt())
            .build();
    }

    public void cancel(String paymentKey, String cancelReason) {
        Map<String, String> body = Map.of("cancelReason", cancelReason);

        tossRestClient.post()
            .uri(CANCEL_PATH, paymentKey)
            .body(body)
            .retrieve()
            .onStatus(HttpStatusCode::isError, (req, res) -> {
                log.warn("토스페이먼츠 결제 취소 실패. paymentKey={}, status={}", paymentKey, res.getStatusCode());
                throw new ServiceException(ErrorCode.PAYMENT_CANCEL_FAILED);
            })
            .toBodilessEntity();

        log.info("토스페이먼츠 결제 취소 완료. paymentKey={}", paymentKey);
    }

    private record TossPaymentResponse(
        String paymentKey,
        String orderId,
        Integer totalAmount,
        String method,
        String status,
        LocalDateTime approvedAt
    ) {
    }
}
