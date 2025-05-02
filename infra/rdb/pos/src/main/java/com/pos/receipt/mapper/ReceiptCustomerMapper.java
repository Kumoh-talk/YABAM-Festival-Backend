package com.pos.receipt.mapper;

import com.pos.receipt.entity.ReceiptCustomerEntity;

import domain.pos.receipt.entity.ReceiptCustomer;

public class ReceiptCustomerMapper {
	public static ReceiptCustomer toReceiptCustomerWithOutReceipt(ReceiptCustomerEntity receiptCustomerEntity) {
		return ReceiptCustomer.of(
			receiptCustomerEntity.getId(),
			receiptCustomerEntity.getCustomerId(),
			null);
	}
}
