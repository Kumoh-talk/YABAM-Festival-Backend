package com.pg.toss.adapter;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.pg.toss.client.TossPaymentClient;
import com.pg.toss.config.TossPaymentProperties;

import domain.pos.payment.entity.PaymentStatus;
import domain.pos.payment.entity.TossConfirmResult;
import domain.pos.payment.port.required.TossPaymentPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentAdapter implements TossPaymentPort {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final TossPaymentClient tossPaymentClient;
    private final TossPaymentProperties properties;

    @Override
    public TossConfirmResult confirm(String paymentKey, String orderId, Integer amount) {
        return tossPaymentClient.confirm(paymentKey, orderId, amount);
    }

    @Override
    public PaymentStatus cancel(String paymentKey, String cancelReason, Integer cancelAmount) {
        return tossPaymentClient.cancel(paymentKey, cancelReason, cancelAmount);
    }

    @Override
    public TossConfirmResult getPayment(String paymentKey) {
        return tossPaymentClient.getPayment(paymentKey);
    }

    @Override
    public void verifyWebhookSignature(String rawBody, String signature) {
        if (signature == null) {
            log.warn("토스페이먼츠 웹훅 서명 헤더 누락");
            throw new ServiceException(ErrorCode.PAYMENT_WEBHOOK_INVALID_SIGNATURE);
        }

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            byte[] keyBytes = properties.getSecretKey().getBytes(StandardCharsets.UTF_8);
            mac.init(new SecretKeySpec(keyBytes, HMAC_ALGORITHM));
            byte[] hmacBytes = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String computed = Base64.getEncoder().encodeToString(hmacBytes);

            if (!computed.equals(signature)) {
                log.warn("토스페이먼츠 웹훅 서명 불일치");
                throw new ServiceException(ErrorCode.PAYMENT_WEBHOOK_INVALID_SIGNATURE);
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("토스페이먼츠 웹훅 서명 검증 중 오류 발생", e);
            throw new ServiceException(ErrorCode.PAYMENT_WEBHOOK_INVALID_SIGNATURE);
        }
    }
}
