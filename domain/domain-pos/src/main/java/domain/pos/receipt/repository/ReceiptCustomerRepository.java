package domain.pos.receipt.repository;

import domain.pos.receipt.entity.ReceiptCustomer;

public interface ReceiptCustomerRepository {
	ReceiptCustomer postReceiptCustomer(Long customerId, Long receiptId);
}
