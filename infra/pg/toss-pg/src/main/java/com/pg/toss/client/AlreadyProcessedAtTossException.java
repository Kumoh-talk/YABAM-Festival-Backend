package com.pg.toss.client;

/**
 * 토스페이먼츠가 ALREADY_PROCESSED_PAYMENT 에러를 반환했을 때 사용하는 모듈 내부 예외.
 * TossPaymentAdapter 에서만 처리하며 도메인 레이어로 전파되지 않는다.
 */
public class AlreadyProcessedAtTossException extends RuntimeException {

	private final String paymentKey;

	public AlreadyProcessedAtTossException(String paymentKey) {
		super("ALREADY_PROCESSED_PAYMENT: paymentKey=" + paymentKey);
		this.paymentKey = paymentKey;
	}

	public String getPaymentKey() {
		return paymentKey;
	}
}
