package domain.pos.receipt.implement;

import java.util.List;

import org.springframework.stereotype.Component;

import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.repository.ReceiptRepository;
import domain.pos.store.entity.Sale;
import domain.pos.table.entity.Table;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReceiptWriter {
	private final ReceiptRepository receiptRepository;

	public Receipt createReceipt(Table table, Sale sale) {
		return receiptRepository.createReceipt(table, sale);
	}

	public List<Receipt> stopReceiptsWithMenu(List<Receipt> receipts) {
		return receiptRepository.stopReceiptsWithMenu(receipts);
	}

	public void restartReceipts(List<Receipt> receipts) {
		receiptRepository.restartReceipts(receipts);
	}

	public void adjustReceipts(List<Receipt> receipts) {
		receiptRepository.adjustReceipts(receipts);
	}

	public void deleteReceipt(Long receiptId) {
		receiptRepository.deleteReceipt(receiptId);
	}
}
