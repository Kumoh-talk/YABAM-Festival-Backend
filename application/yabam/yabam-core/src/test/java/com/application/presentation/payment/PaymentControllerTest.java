package com.application.presentation.payment;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.application.presentation.payment.controller.PaymentController;
import com.config.WebMvcConfig;
import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.exception.handler.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vo.UserPassport;
import com.vo.UserRole;

import domain.pos.payment.entity.Payment;
import domain.pos.payment.entity.PaymentStatus;
import domain.pos.payment.entity.TossConfirmResult;
import domain.pos.payment.port.required.TossPaymentPort;
import domain.pos.payment.service.PaymentService;

@WebMvcTest(PaymentController.class)
@Import({WebMvcConfig.class, GlobalExceptionHandler.class})
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private TossPaymentPort tossPaymentPort;

    private static final String PAYMENT_KEY = "toss_payment_key_test_1234567890";
    private static final String ORDER_ID = "123e4567-e89b-12d3-a456-426614174000";
    private static final UUID RECEIPT_ID = UUID.fromString(ORDER_ID);
    private static final Integer AMOUNT = 10000;

    private Payment samplePayment() {
        return Payment.builder()
            .paymentId(1L)
            .receiptId(RECEIPT_ID)
            .tossPaymentKey(PAYMENT_KEY)
            .tossOrderId(ORDER_ID)
            .amount(AMOUNT)
            .status(PaymentStatus.DONE)
            .paymentMethod("카드")
            .approvedAt(LocalDateTime.of(2024, 6, 1, 12, 0, 0))
            .build();
    }

    private TossConfirmResult sampleTossResult() {
        return TossConfirmResult.builder()
            .tossPaymentKey(PAYMENT_KEY)
            .tossOrderId(ORDER_ID)
            .amount(AMOUNT)
            .status(PaymentStatus.DONE)
            .paymentMethod("카드")
            .approvedAt(LocalDateTime.of(2024, 6, 1, 12, 0, 0))
            .build();
    }

    private String ownerPassportHeader() throws Exception {
        UserPassport passport = UserPassport.of(1L, "점주", UserRole.ROLE_OWNER);
        String json = objectMapper.writeValueAsString(passport);
        return URLEncoder.encode(json, StandardCharsets.UTF_8);
    }

    @Nested
    @DisplayName("POST /api/v1/payments/toss/confirm")
    class ConfirmPayment {

        @Test
        void 성공() throws Exception {
            // given
            given(paymentService.confirmPayment(PAYMENT_KEY, ORDER_ID, AMOUNT))
                .willReturn(samplePayment());

            String body = """
                {
                    "paymentKey": "%s",
                    "orderId": "%s",
                    "amount": %d
                }
                """.formatted(PAYMENT_KEY, ORDER_ID, AMOUNT);

            // when & then
            mockMvc.perform(post("/api/v1/payments/toss/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tossPaymentKey").value(PAYMENT_KEY))
                .andExpect(jsonPath("$.data.status").value("DONE"))
                .andExpect(jsonPath("$.data.amount").value(AMOUNT));
        }

        @Test
        void 실패_필수값_누락() throws Exception {
            // given - paymentKey 없음
            String body = """
                {
                    "orderId": "%s",
                    "amount": %d
                }
                """.formatted(ORDER_ID, AMOUNT);

            // when & then
            mockMvc.perform(post("/api/v1/payments/toss/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest());
        }

        @Test
        void 실패_이미_결제된_영수증() throws Exception {
            // given
            given(paymentService.confirmPayment(any(), any(), any()))
                .willThrow(new ServiceException(ErrorCode.ALREADY_PAID_RECEIPT));

            String body = """
                {
                    "paymentKey": "%s",
                    "orderId": "%s",
                    "amount": %d
                }
                """.formatted(PAYMENT_KEY, ORDER_ID, AMOUNT);

            // when & then
            mockMvc.perform(post("/api/v1/payments/toss/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/payments/{paymentKey}/cancel")
    class CancelPayment {

        @Test
        void 성공_전액취소() throws Exception {
            // given
            willDoNothing().given(paymentService).cancelPayment(eq(PAYMENT_KEY), any(), isNull(), any());

            String body = """
                {"cancelReason": "고객 요청"}
                """;

            // when & then
            mockMvc.perform(post("/api/v1/payments/{paymentKey}/cancel", PAYMENT_KEY)
                    .header("X-User-Info", ownerPassportHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk());
        }

        @Test
        void 성공_부분취소() throws Exception {
            // given
            willDoNothing().given(paymentService).cancelPayment(eq(PAYMENT_KEY), any(), eq(3000), any());

            String body = """
                {"cancelReason": "부분 환불", "cancelAmount": 3000}
                """;

            // when & then
            mockMvc.perform(post("/api/v1/payments/{paymentKey}/cancel", PAYMENT_KEY)
                    .header("X-User-Info", ownerPassportHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk());
        }

        @Test
        void 실패_권한_없음() throws Exception {
            // given
            UserPassport userPassport = UserPassport.of(1L, "일반유저", UserRole.ROLE_USER);
            String json = objectMapper.writeValueAsString(userPassport);
            String encodedHeader = URLEncoder.encode(json, StandardCharsets.UTF_8);

            String body = """
                {"cancelReason": "고객 요청"}
                """;

            // when & then
            mockMvc.perform(post("/api/v1/payments/{paymentKey}/cancel", PAYMENT_KEY)
                    .header("X-User-Info", encodedHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/payments/receipts/{receiptId}")
    class GetPaymentByReceipt {

        @Test
        void 결제_있을때_반환() throws Exception {
            // given
            given(paymentService.findPaymentByReceiptId(RECEIPT_ID))
                .willReturn(Optional.of(samplePayment()));

            // when & then
            mockMvc.perform(get("/api/v1/payments/receipts/{receiptId}", RECEIPT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tossPaymentKey").value(PAYMENT_KEY));
        }

        @Test
        void 결제_없을때_null_반환() throws Exception {
            // given
            given(paymentService.findPaymentByReceiptId(RECEIPT_ID))
                .willReturn(Optional.empty());

            // when & then
            mockMvc.perform(get("/api/v1/payments/receipts/{receiptId}", RECEIPT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/payments/toss/{paymentKey}")
    class GetTossPayment {

        @Test
        void 성공() throws Exception {
            // given
            given(paymentService.getPaymentFromToss(PAYMENT_KEY))
                .willReturn(sampleTossResult());

            // when & then
            mockMvc.perform(get("/api/v1/payments/toss/{paymentKey}", PAYMENT_KEY)
                    .header("X-User-Info", ownerPassportHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tossPaymentKey").value(PAYMENT_KEY))
                .andExpect(jsonPath("$.data.status").value("DONE"))
                .andExpect(jsonPath("$.data.amount").value(AMOUNT));
        }

        @Test
        void 실패_권한_없음() throws Exception {
            // given
            UserPassport userPassport = UserPassport.of(1L, "일반유저", UserRole.ROLE_USER);
            String json = objectMapper.writeValueAsString(userPassport);
            String encodedHeader = URLEncoder.encode(json, StandardCharsets.UTF_8);

            // when & then
            mockMvc.perform(get("/api/v1/payments/toss/{paymentKey}", PAYMENT_KEY)
                    .header("X-User-Info", encodedHeader))
                .andExpect(status().isForbidden());
        }

        @Test
        void 실패_결제키_없음() throws Exception {
            // given
            given(paymentService.getPaymentFromToss(PAYMENT_KEY))
                .willThrow(new ServiceException(ErrorCode.PAYMENT_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/v1/payments/toss/{paymentKey}", PAYMENT_KEY)
                    .header("X-User-Info", ownerPassportHeader()))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/payments/toss/webhook")
    class HandleWebhook {

        private static final String VALID_SIGNATURE = "valid-hmac-signature";

        @Test
        void 성공_서명_유효_PAYMENT_STATUS_CHANGED() throws Exception {
            // given
            willDoNothing().given(tossPaymentPort).verifyWebhookSignature(any(), eq(VALID_SIGNATURE));
            willDoNothing().given(paymentService).processWebhook(any(), any());

            String body = """
                {
                    "eventType": "PAYMENT_STATUS_CHANGED",
                    "createdAt": "2024-06-01T12:00:00+09:00",
                    "data": {
                        "paymentKey": "%s",
                        "orderId": "%s",
                        "status": "CANCELED"
                    }
                }
                """.formatted(PAYMENT_KEY, ORDER_ID);

            // when & then
            mockMvc.perform(post("/api/v1/payments/toss/webhook")
                    .header("TossPayments-Signature", VALID_SIGNATURE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk());

            then(paymentService).should().processWebhook(PAYMENT_KEY, "CANCELED");
        }

        @Test
        void 실패_서명_불일치() throws Exception {
            // given
            willThrow(new ServiceException(ErrorCode.PAYMENT_WEBHOOK_INVALID_SIGNATURE))
                .given(tossPaymentPort).verifyWebhookSignature(any(), any());

            String body = """
                {
                    "eventType": "PAYMENT_STATUS_CHANGED",
                    "createdAt": "2024-06-01T12:00:00+09:00",
                    "data": {
                        "paymentKey": "%s",
                        "orderId": "%s",
                        "status": "CANCELED"
                    }
                }
                """.formatted(PAYMENT_KEY, ORDER_ID);

            // when & then
            mockMvc.perform(post("/api/v1/payments/toss/webhook")
                    .header("TossPayments-Signature", "wrong-signature")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isUnauthorized());

            then(paymentService).should(never()).processWebhook(any(), any());
        }

        @Test
        void 무시_알수없는_eventType() throws Exception {
            // given
            willDoNothing().given(tossPaymentPort).verifyWebhookSignature(any(), any());

            String body = """
                {
                    "eventType": "UNKNOWN_EVENT",
                    "createdAt": "2024-06-01T12:00:00+09:00",
                    "data": {
                        "paymentKey": "%s",
                        "orderId": "%s",
                        "status": "DONE"
                    }
                }
                """.formatted(PAYMENT_KEY, ORDER_ID);

            // when & then
            mockMvc.perform(post("/api/v1/payments/toss/webhook")
                    .header("TossPayments-Signature", VALID_SIGNATURE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk());

            then(paymentService).should(never()).processWebhook(any(), any());
        }
    }
}
