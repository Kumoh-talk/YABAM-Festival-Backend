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
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private static final String QUERY_PATH = "/v1/payments/{paymentKey}";

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    public TossConfirmResult confirm(String paymentKey, String orderId, Integer amount) {
        Map<String, Object> body = Map.of(
            "paymentKey", paymentKey,
            "orderId", orderId,
            "amount", amount
        );

        TossPaymentResponse response = tossRestClient.post()
            .uri(CONFIRM_PATH)
            .header("Idempotency-Key", orderId)
            .body(body)
            .retrieve()
            .onStatus(HttpStatusCode::isError, (req, res) -> {
                String rawBody = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                log.warn("토스페이먼츠 결제 승인 실패. status={}, body={}", res.getStatusCode(), rawBody);
                throw resolveConfirmError(rawBody);
            })
            .body(TossPaymentResponse.class);

        if (response == null) {
            throw new ServiceException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        return buildResult(response);
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
                String rawBody = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                log.warn("토스페이먼츠 결제 취소 실패. paymentKey={}, status={}, body={}",
                    paymentKey, res.getStatusCode(), rawBody);
                throw resolveCancelError(rawBody);
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

    public TossConfirmResult getPayment(String paymentKey) {
        TossPaymentResponse response = tossRestClient.get()
            .uri(QUERY_PATH, paymentKey)
            .retrieve()
            .onStatus(HttpStatusCode::isError, (req, res) -> {
                String rawBody = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                log.warn("토스페이먼츠 결제 조회 실패. paymentKey={}, status={}, body={}",
                    paymentKey, res.getStatusCode(), rawBody);
                throw new ServiceException(ErrorCode.PAYMENT_NOT_FOUND);
            })
            .body(TossPaymentResponse.class);

        if (response == null) {
            throw new ServiceException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        return buildResult(response);
    }

    private TossConfirmResult buildResult(TossPaymentResponse response) {
        return TossConfirmResult.builder()
            .tossPaymentKey(response.paymentKey())
            .tossOrderId(response.orderId())
            .amount(response.totalAmount())
            .status(parseStatus(response.status()))
            .paymentMethod(response.method())
            .approvedAt(response.approvedAt() != null ? response.approvedAt().toLocalDateTime() : null)
            .build();
    }

    private ServiceException resolveConfirmError(String rawBody) {
        TossErrorResponse error = parseError(rawBody);
        if (error == null) {
            return new ServiceException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }
        return switch (error.code()) {
            case "ALREADY_PROCESSED_PAYMENT" -> new ServiceException(ErrorCode.ALREADY_PAID_RECEIPT);
            case "AMOUNT_MISMATCH" -> new ServiceException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
            default -> {
                log.warn("토스페이먼츠 알 수 없는 승인 오류. code={}, message={}", error.code(), error.message());
                yield new ServiceException(ErrorCode.PAYMENT_CONFIRM_FAILED);
            }
        };
    }

    private ServiceException resolveCancelError(String rawBody) {
        TossErrorResponse error = parseError(rawBody);
        if (error == null) {
            return new ServiceException(ErrorCode.PAYMENT_CANCEL_FAILED);
        }
        return switch (error.code()) {
            case "ALREADY_CANCELED_PAYMENT" -> new ServiceException(ErrorCode.PAYMENT_CANCEL_FAILED);
            case "EXCEED_MAX_REFUND_AMOUNT" -> new ServiceException(ErrorCode.PAYMENT_CANCEL_AMOUNT_EXCEEDED);
            default -> {
                log.warn("토스페이먼츠 알 수 없는 취소 오류. code={}, message={}", error.code(), error.message());
                yield new ServiceException(ErrorCode.PAYMENT_CANCEL_FAILED);
            }
        };
    }

    private TossErrorResponse parseError(String rawBody) {
        try {
            return objectMapper.readValue(rawBody, TossErrorResponse.class);
        } catch (Exception e) {
            log.warn("토스페이먼츠 에러 응답 파싱 실패. rawBody={}", rawBody);
            return null;
        }
    }

    private PaymentStatus parseStatus(String status) {
        if (status == null) {
            log.warn("토스페이먼츠 응답에 status 필드가 없습니다.");
            throw new ServiceException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }
        try {
            return PaymentStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 토스 결제 상태값. status={}", status);
            throw new ServiceException(ErrorCode.PAYMENT_CONFIRM_FAILED);
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

    private record TossErrorResponse(String code, String message) {
    }
}
