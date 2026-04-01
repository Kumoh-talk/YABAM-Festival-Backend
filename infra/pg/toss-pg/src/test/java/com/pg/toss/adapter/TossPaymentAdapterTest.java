package com.pg.toss.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.pg.toss.client.AlreadyProcessedAtTossException;
import com.pg.toss.client.TossPaymentClient;
import com.pg.toss.config.TossPaymentProperties;

import domain.pos.payment.entity.PaymentStatus;
import domain.pos.payment.entity.TossConfirmResult;

@ExtendWith(MockitoExtension.class)
class TossPaymentAdapterTest {

    private static final String TEST_SECRET_KEY = "test_sk_webhook_secret_key_1234";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Mock
    private TossPaymentClient tossPaymentClient;

    @Mock
    private TossPaymentProperties properties;

    private TossPaymentAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TossPaymentAdapter(tossPaymentClient, properties);
    }

    @Nested
    @DisplayName("결제 승인 — ALREADY_PROCESSED_PAYMENT 복구")
    class ConfirmAlreadyProcessed {

        private static final String PAYMENT_KEY = "toss_payment_key_test_1234567890";
        private static final String ORDER_ID = "123e4567-e89b-12d3-a456-426614174000";
        private static final Integer AMOUNT = 10000;

        @Test
        void ALREADY_PROCESSED_PAYMENT_수신시_getPayment로_복구하여_반환() {
            TossConfirmResult recoveredResult = TossConfirmResult.builder()
                .tossPaymentKey(PAYMENT_KEY)
                .tossOrderId(ORDER_ID)
                .amount(AMOUNT)
                .status(PaymentStatus.DONE)
                .paymentMethod("카드")
                .approvedAt(null)
                .build();

            given(tossPaymentClient.confirm(PAYMENT_KEY, ORDER_ID, AMOUNT))
                .willThrow(new AlreadyProcessedAtTossException(PAYMENT_KEY));
            given(tossPaymentClient.getPayment(PAYMENT_KEY))
                .willReturn(recoveredResult);

            TossConfirmResult result = adapter.confirm(PAYMENT_KEY, ORDER_ID, AMOUNT);

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.DONE);
            assertThat(result.getTossPaymentKey()).isEqualTo(PAYMENT_KEY);
            then(tossPaymentClient).should().getPayment(PAYMENT_KEY);
        }

        @Test
        void ALREADY_PROCESSED_PAYMENT_후_getPayment_실패시_예외_전파() {
            given(tossPaymentClient.confirm(PAYMENT_KEY, ORDER_ID, AMOUNT))
                .willThrow(new AlreadyProcessedAtTossException(PAYMENT_KEY));
            given(tossPaymentClient.getPayment(PAYMENT_KEY))
                .willThrow(new ServiceException(ErrorCode.PAYMENT_NOT_FOUND));

            // when / then
            assertThatThrownBy(() -> adapter.confirm(PAYMENT_KEY, ORDER_ID, AMOUNT))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("웹훅 서명 검증")
    class VerifyWebhookSignature {

        private static final String RAW_BODY =
            "{\"eventType\":\"PAYMENT_STATUS_CHANGED\","
            + "\"createdAt\":\"2024-06-01T12:00:00+09:00\","
            + "\"data\":{\"paymentKey\":\"test_key\",\"orderId\":\"order_1\",\"status\":\"CANCELED\"}}";

        @Test
        void 성공_유효한_서명() throws Exception {
            given(properties.getSecretKey()).willReturn(TEST_SECRET_KEY);
            String validSignature = computeHmac(TEST_SECRET_KEY, RAW_BODY);

            assertThatNoException().isThrownBy(
                () -> adapter.verifyWebhookSignature(RAW_BODY, validSignature));
        }

        @Test
        void 실패_서명_불일치() {
            given(properties.getSecretKey()).willReturn(TEST_SECRET_KEY);

            assertThatThrownBy(() -> adapter.verifyWebhookSignature(RAW_BODY, "wrong-signature"))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                    ErrorCode.PAYMENT_WEBHOOK_INVALID_SIGNATURE);
        }

        @Test
        void 실패_서명_헤더_누락() {
            assertThatThrownBy(() -> adapter.verifyWebhookSignature(RAW_BODY, null))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                    ErrorCode.PAYMENT_WEBHOOK_INVALID_SIGNATURE);
        }

        @Test
        void 실패_다른_시크릿키로_생성된_서명() throws Exception {
            given(properties.getSecretKey()).willReturn(TEST_SECRET_KEY);
            String signatureFromDifferentKey = computeHmac("different_secret_key", RAW_BODY);

            assertThatThrownBy(
                () -> adapter.verifyWebhookSignature(RAW_BODY, signatureFromDifferentKey))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                    ErrorCode.PAYMENT_WEBHOOK_INVALID_SIGNATURE);
        }

        @Test
        void 실패_변조된_바디() throws Exception {
            given(properties.getSecretKey()).willReturn(TEST_SECRET_KEY);
            String validSignature = computeHmac(TEST_SECRET_KEY, RAW_BODY);
            String tamperedBody = RAW_BODY.replace("CANCELED", "DONE");

            assertThatThrownBy(() -> adapter.verifyWebhookSignature(tamperedBody, validSignature))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                    ErrorCode.PAYMENT_WEBHOOK_INVALID_SIGNATURE);
        }
    }

    private String computeHmac(String secretKey, String rawBody)
        throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        return Base64.getEncoder().encodeToString(mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8)));
    }
}
