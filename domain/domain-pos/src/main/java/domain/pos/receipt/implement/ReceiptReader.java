package domain.pos.receipt.implement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.receipt.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReceiptReader {
	private final ReceiptRepository receiptRepository;

	public boolean existsReceipt(Long receiptId) {
		return receiptRepository.existsReceipt(receiptId);
	}

	public Optional<ReceiptInfo> getReceiptInfo(Long receiptId) {
		return receiptRepository.getReceiptInfo(receiptId);
	}

	public Optional<Receipt> getReceiptWithTableAndStore(Long receiptId) {
		return receiptRepository.getReceiptWithTableAndStore(receiptId);
	}

	public List<Receipt> getStopReceiptsWithStore(List<Long> receiptIds) {
		return receiptRepository.getStopReceiptsWithStore(receiptIds);
	}

	public List<Receipt> getStopReceiptsWithTableAndStore(List<Long> receiptIds) {
		return receiptRepository.getStopReceiptsWithTableAndStore(receiptIds);
	}

	public List<Receipt> getNonStopReceiptsWithStoreAndLock(List<Long> receiptIds) {
		return receiptRepository.getNonStopReceiptsWithStoreAndLock(receiptIds);
	}

	public Page<ReceiptInfo> getAdjustedReceiptPageBySale(Pageable pageable, Long saleId) {
		return receiptRepository.getAdjustedReceiptPageBySale(pageable, saleId);
	}

	public ReceiptInfo getNonAdjustReceipt(Long tableId) {
		return receiptRepository.getNonAdjustReceipt(tableId);
	}

	public Slice<Receipt> getCustomerReceiptSlice(Pageable pageable, Long customerId) {
		return receiptRepository.getCustomerReceiptSlice(pageable, customerId);
	}
}
