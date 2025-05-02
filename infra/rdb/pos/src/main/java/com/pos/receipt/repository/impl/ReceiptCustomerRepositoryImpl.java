package com.pos.receipt.repository.impl;

import org.springframework.stereotype.Repository;

import com.pos.receipt.entity.ReceiptCustomerEntity;
import com.pos.receipt.repository.jpa.ReceiptCustomerJpaRepository;

import domain.pos.receipt.repository.ReceiptCustomerRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReceiptCustomerRepositoryImpl implements ReceiptCustomerRepository {
	private final ReceiptCustomerJpaRepository receiptCustomerJpaRepository;

	@Override
	public void postReceiptCustomer(Long customerId, Long receiptId) {
		ReceiptCustomerEntity receiptCustomerEntity = ReceiptCustomerEntity.of(customerId, receiptId);
		receiptCustomerJpaRepository.save(receiptCustomerEntity);
	}
}
