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
import com.pg.toss.client.TossPaymentClient;
import com.pg.toss.config.TossPaymentProperties;

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
    @DisplayName("웹훅 서명 검증")
    class VerifyWebhookSignature {

        private static final String RAW_BODY =
            "{\"eventType\":\"PAYMENT_STATUS_CHANGED\","
            + "\"createdAt\":\"2024-06-01T12:00:00+09:00\","
            + "\"data\":{\"paymentKey\":\"test_key\",\"orderId\":\"order_1\",\"status\":\"CANCELED\"}}";

        @Test
        void 성공_유효한_서명() throws Exception {
            // given
            given(properties.getSecretKey()).willReturn(TEST_SECRET_KEY);
            String validSignature = computeHmac(TEST_SECRET_KEY, RAW_BODY);

            // when & then
            assertThatNoException().isThrownBy(
                () -> adapter.verifyWebhookSignature(RAW_BODY, validSignature));
        }

        @Test
        void 실패_서명_불일치() {
            // given
            given(properties.getSecretKey()).willReturn(TEST_SECRET_KEY);

            // when & then
            assertThatThrownBy(() -> adapter.verifyWebhookSignature(RAW_BODY, "wrong-signature"))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                    ErrorCode.PAYMENT_WEBHOOK_INVALID_SIGNATURE);
        }

        @Test
        void 실패_서명_헤더_누락() {
            // when & then
            assertThatThrownBy(() -> adapter.verifyWebhookSignature(RAW_BODY, null))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                    ErrorCode.PAYMENT_WEBHOOK_INVALID_SIGNATURE);
        }

        @Test
        void 실패_다른_시크릿키로_생성된_서명() throws Exception {
            // given
            given(properties.getSecretKey()).willReturn(TEST_SECRET_KEY);
            String signatureFromDifferentKey = computeHmac("different_secret_key", RAW_BODY);

            // when & then
            assertThatThrownBy(
                () -> adapter.verifyWebhookSignature(RAW_BODY, signatureFromDifferentKey))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                    ErrorCode.PAYMENT_WEBHOOK_INVALID_SIGNATURE);
        }

        @Test
        void 실패_변조된_바디() throws Exception {
            // given
            given(properties.getSecretKey()).willReturn(TEST_SECRET_KEY);
            String validSignature = computeHmac(TEST_SECRET_KEY, RAW_BODY);
            String tamperedBody = RAW_BODY.replace("CANCELED", "DONE");

            // when & then
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
