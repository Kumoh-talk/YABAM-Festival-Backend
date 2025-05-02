package domain.pos.receipt.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptCustomerRepository {
	void postReceiptCustomer(Long customerId, Long receiptId);
}
