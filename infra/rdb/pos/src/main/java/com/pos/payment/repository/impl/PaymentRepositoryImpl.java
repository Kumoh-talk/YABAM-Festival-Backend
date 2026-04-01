package com.pos.payment.repository.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.pos.payment.entity.PaymentEntity;
import com.pos.payment.mapper.PaymentMapper;
import com.pos.payment.repository.jpa.PaymentJpaRepository;

import domain.pos.payment.entity.Payment;
import domain.pos.payment.entity.PaymentStatus;
import domain.pos.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

	private final PaymentJpaRepository paymentJpaRepository;

	@Override
	public Payment save(Payment payment) {
		PaymentEntity entity = PaymentMapper.toEntity(payment);
		PaymentEntity saved = paymentJpaRepository.save(entity);
		return PaymentMapper.toDomain(saved);
	}

	@Override
	public Optional<Payment> findByReceiptId(UUID receiptId) {
		return paymentJpaRepository.findByReceiptId(receiptId)
			.map(PaymentMapper::toDomain);
	}

	@Override
	public Optional<Payment> findByTossPaymentKey(String tossPaymentKey) {
		return paymentJpaRepository.findByTossPaymentKey(tossPaymentKey)
			.map(PaymentMapper::toDomain);
	}

	@Override
	@Transactional
	public Payment updateStatus(Long paymentId, PaymentStatus status) {
		PaymentEntity entity = paymentJpaRepository.findById(paymentId)
			.orElseThrow(() -> new ServiceException(ErrorCode.PAYMENT_NOT_FOUND));
		entity.updateStatus(status);
		return PaymentMapper.toDomain(entity);
	}

	@Override
	public Payment updateConfirmResult(Long paymentId, PaymentStatus status, String paymentMethod,
		LocalDateTime approvedAt) {
		PaymentEntity entity = paymentJpaRepository.findById(paymentId)
			.orElseThrow(() -> new ServiceException(ErrorCode.PAYMENT_NOT_FOUND));
		entity.updateConfirmResult(status, paymentMethod, approvedAt);
		return PaymentMapper.toDomain(entity);
	}

	@Override
	public void delete(Long paymentId) {
		paymentJpaRepository.deleteById(paymentId);
	}

	@Override
	public List<Payment> findBySaleId(Long saleId) {
		return paymentJpaRepository.findBySaleId(saleId).stream()
			.map(PaymentMapper::toDomain)
			.toList();
	}

	@Override
	public List<Payment> findInProgressOlderThan(LocalDateTime threshold) {
		return paymentJpaRepository
			.findByStatusAndCreatedAtBefore(PaymentStatus.IN_PROGRESS, threshold)
			.stream().map(PaymentMapper::toDomain).toList();
	}
}
