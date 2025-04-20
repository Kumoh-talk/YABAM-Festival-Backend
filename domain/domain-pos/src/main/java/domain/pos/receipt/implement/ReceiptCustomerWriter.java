package domain.pos.receipt.implement;

import org.springframework.stereotype.Component;

import domain.pos.receipt.entity.ReceiptCustomer;
import domain.pos.receipt.repository.ReceiptCustomerRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReceiptCustomerWriter {
	private final ReceiptCustomerRepository receiptCustomerRepository;

	public ReceiptCustomer postReceiptCustomer(Long customerId, Long receiptId) {
		return receiptCustomerRepository.postReceiptCustomer(customerId, receiptId);
	}
}
