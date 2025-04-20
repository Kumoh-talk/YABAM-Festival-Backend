package domain.pos.receipt.entity;

import lombok.Getter;

@Getter
public class ReceiptCustomer {
	private final Long receiptCustomerId;
	private final Long customerId;
	private final Receipt receipt;

	private ReceiptCustomer(Long receiptCustomerId, Long customerId, Receipt receipt) {
		this.receiptCustomerId = receiptCustomerId;
		this.customerId = customerId;
		this.receipt = receipt;
	}

	public static ReceiptCustomer of(Long receiptCustomerId, Long customerId, Receipt receipt) {
		return new ReceiptCustomer(receiptCustomerId, customerId, receipt);
	}
}
