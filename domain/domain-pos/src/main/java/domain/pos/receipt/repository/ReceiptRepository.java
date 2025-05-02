package domain.pos.receipt.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.store.entity.Sale;
import domain.pos.table.entity.Table;

@Repository
public interface ReceiptRepository {
	Receipt createReceipt(Table table, Sale sale);

	Optional<ReceiptInfo> getReceiptInfo(Long receiptId);

	Optional<Receipt> getReceiptWithTableAndStore(Long receiptId);

	List<Receipt> getStopReceiptsWithTableAndStore(List<Long> receiptIds);

	List<Receipt> getStopReceiptsWithStore(List<Long> receiptIds);

	List<Receipt> getNonStopReceiptsWithStoreAndLock(List<Long> receiptIds);

	Page<ReceiptInfo> getAdjustedReceiptPageBySale(Pageable pageable, Long saleId);

	ReceiptInfo getNonAdjustReceipt(Long tableId);

	Slice<Receipt> getCustomerReceiptSlice(Pageable pageable, Long customerId);

	boolean existsReceipt(Long receiptId);

	List<Receipt> stopReceiptsWithMenu(List<Receipt> patchReceipts);

	void restartReceipts(List<Receipt> receipts);

	void adjustReceipts(List<Receipt> receipts);

	void deleteReceipt(Long receiptId);
}
