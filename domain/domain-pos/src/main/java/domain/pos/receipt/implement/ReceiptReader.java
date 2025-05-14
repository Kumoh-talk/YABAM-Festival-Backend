package domain.pos.receipt.implement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

	public boolean existsReceipt(UUID receiptId) {
		return receiptRepository.existsReceipt(receiptId);
	}

	public Optional<ReceiptInfo> getReceiptInfo(UUID receiptId) {
		return receiptRepository.getReceiptInfo(receiptId);
	}

	public Optional<Receipt> getReceiptWithTableAndStore(UUID receiptId) {
		return receiptRepository.getReceiptWithTableAndStore(receiptId);
	}

	public List<Receipt> getStopReceiptsWithStore(List<UUID> receiptIds) {
		return receiptRepository.getStopReceiptsWithStore(receiptIds);
	}

	public List<Receipt> getStopReceiptsWithTableAndStore(List<UUID> receiptIds) {
		return receiptRepository.getStopReceiptsWithTableAndStore(receiptIds);
	}

	public Optional<Receipt> getNonStopReceiptsWithTableAndStoreAndLock(UUID receiptId) {
		return receiptRepository.getNonStopReceiptsWithTableAndStoreAndLock(receiptId);
	}

	public List<Receipt> getNonStopReceiptsWithStoreAndLock(List<UUID> receiptIds) {
		return receiptRepository.getNonStopReceiptsWithStoreAndLock(receiptIds);
	}

	public Page<ReceiptInfo> getAdjustedReceiptPageBySale(Pageable pageable, Long saleId) {
		return receiptRepository.getAdjustedReceiptPageBySale(pageable, saleId);
	}

	public ReceiptInfo getNonAdjustReceipt(Long tableId) {
		return receiptRepository.getNonAdjustReceipt(tableId);
	}

	public Slice<Receipt> getCustomerReceiptSlice(int pageSize, UUID lastReceiptId, Long customerId) {
		return receiptRepository.getCustomerReceiptSlice(pageSize, lastReceiptId, customerId);
	}
}
