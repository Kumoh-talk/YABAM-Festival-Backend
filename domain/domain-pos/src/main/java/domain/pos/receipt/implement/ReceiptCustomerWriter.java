package domain.pos.receipt.implement;

import java.util.UUID;

import org.springframework.stereotype.Component;

import domain.pos.receipt.port.required.ReceiptCustomerRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReceiptCustomerWriter {
	private final ReceiptCustomerRepository receiptCustomerRepository;

	public void postReceiptCustomer(Long customerId, UUID receiptId) {
		receiptCustomerRepository.postReceiptCustomer(customerId, receiptId);
	}
}
