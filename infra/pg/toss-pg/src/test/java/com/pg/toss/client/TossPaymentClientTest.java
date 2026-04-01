package com.pg.toss.client;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pg.toss.config.TossPaymentProperties;

@ExtendWith(MockitoExtension.class)
class TossPaymentClientTest {

	private static final String PAYMENT_KEY = "toss_payment_key_test_1234567890";
	private static final String ORDER_ID = "123e4567-e89b-12d3-a456-426614174000";
	private static final Integer AMOUNT = 10000;

	@Mock
	private RestClient tossRestClient;

	@Mock
	private TossPaymentProperties properties;

	private TossPaymentClient tossPaymentClient;

	@BeforeEach
	void setUp() {
		tossPaymentClient = new TossPaymentClient(tossRestClient, new ObjectMapper(), properties);
	}

	@Nested
	@DisplayName("결제 승인 에러 코드 매핑 (resolveConfirmError)")
	class ResolveConfirmError {

		@Test
		void ALREADY_PROCESSED_PAYMENT는_AlreadyProcessedAtTossException() throws Exception {
			String body = "{\"code\":\"ALREADY_PROCESSED_PAYMENT\",\"message\":\"" + PAYMENT_KEY + "\"}";

			RuntimeException result = invokeResolveConfirmError(body);

			assertThat(result).isInstanceOf(AlreadyProcessedAtTossException.class);
		}

		@Test
		void AMOUNT_MISMATCH는_PAYMENT_AMOUNT_MISMATCH_ServiceException() throws Exception {
			String body = "{\"code\":\"AMOUNT_MISMATCH\",\"message\":\"금액 불일치\"}";

			RuntimeException result = invokeResolveConfirmError(body);

			assertThat(result).isInstanceOf(ServiceException.class);
			assertThat(((ServiceException)result).getErrorCode())
				.isEqualTo(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
		}

		@Test
		void 알수없는_에러코드는_PAYMENT_CONFIRM_FAILED_ServiceException() throws Exception {
			String body = "{\"code\":\"UNKNOWN_ERROR\",\"message\":\"알 수 없는 오류\"}";

			RuntimeException result = invokeResolveConfirmError(body);

			assertThat(result).isInstanceOf(ServiceException.class);
			assertThat(((ServiceException)result).getErrorCode())
				.isEqualTo(ErrorCode.PAYMENT_CONFIRM_FAILED);
		}

		@Test
		void JSON_파싱_불가시_PAYMENT_CONFIRM_FAILED_ServiceException() throws Exception {
			String body = "invalid json";

			RuntimeException result = invokeResolveConfirmError(body);

			assertThat(result).isInstanceOf(ServiceException.class);
			assertThat(((ServiceException)result).getErrorCode())
				.isEqualTo(ErrorCode.PAYMENT_CONFIRM_FAILED);
		}

		private RuntimeException invokeResolveConfirmError(String rawBody) throws Exception {
			Method method = TossPaymentClient.class
				.getDeclaredMethod("resolveConfirmError", String.class);
			method.setAccessible(true);
			return (RuntimeException)method.invoke(tossPaymentClient, rawBody);
		}
	}

	@Nested
	@DisplayName("재전송 소진 복구 (@Recover recoverFromConfirmTimeout)")
	class RecoverFromConfirmTimeout {

		@Test
		void 재전송_소진시_PAYMENT_CONFIRM_TIMEOUT_예외() {
			ResourceAccessException timeoutEx = new ResourceAccessException("timeout");

			assertThatThrownBy(() ->
				tossPaymentClient.recoverFromConfirmTimeout(timeoutEx, PAYMENT_KEY, ORDER_ID, AMOUNT))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_CONFIRM_TIMEOUT);
		}
	}
}
