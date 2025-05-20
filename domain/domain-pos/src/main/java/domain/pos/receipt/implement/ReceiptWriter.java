package domain.pos.receipt.implement;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;

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

	public void deleteReceipt(UUID receiptId) {
		receiptRepository.deleteReceipt(receiptId);
	}

	public void moveReceiptTable(Receipt receipt, Table moveTable) {
		if (receiptRepository.updateReceiptTable(receipt, moveTable) == 0) {
			throw new ServiceException(ErrorCode.FAILED_TO_UPDATE_RECEIPT_TABLE);
		}
	}
}
