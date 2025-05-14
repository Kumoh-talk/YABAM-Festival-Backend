package domain.pos.receipt.repository;

import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptCustomerRepository {
	void postReceiptCustomer(Long customerId, UUID receiptId);
}
