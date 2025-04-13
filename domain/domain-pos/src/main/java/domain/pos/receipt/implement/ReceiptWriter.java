package domain.pos.receipt.implement;

import org.springframework.stereotype.Component;

import domain.pos.member.entity.UserPassport;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.repository.ReceiptRepository;
import domain.pos.store.entity.Sale;
import domain.pos.table.entity.Table;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReceiptWriter {
	private final ReceiptRepository receiptRepository;

	public Receipt createReceipt(UserPassport userPassport, Table table, Sale sale) {
		return receiptRepository.createReceipt(userPassport, table, sale);
	}

	public void adjustReceipt(Long receiptId) {
		receiptRepository.adjustReceipt(receiptId);
	}

	public void deleteReceipt(Long receiptId) {
		receiptRepository.deleteReceipt(receiptId);
	}
}
