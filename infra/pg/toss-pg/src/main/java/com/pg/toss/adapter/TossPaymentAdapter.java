package com.pg.toss.adapter;

import org.springframework.stereotype.Component;

import com.pg.toss.client.TossPaymentClient;

import domain.pos.payment.entity.TossConfirmResult;
import domain.pos.payment.port.required.TossPaymentPort;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TossPaymentAdapter implements TossPaymentPort {

    private final TossPaymentClient tossPaymentClient;

    @Override
    public TossConfirmResult confirm(String paymentKey, String orderId, Integer amount) {
        return tossPaymentClient.confirm(paymentKey, orderId, amount);
    }

    @Override
    public void cancel(String paymentKey, String cancelReason) {
        tossPaymentClient.cancel(paymentKey, cancelReason);
    }
}
