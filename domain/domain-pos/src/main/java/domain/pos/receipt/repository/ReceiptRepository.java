package domain.pos.receipt.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

	Optional<ReceiptInfo> getReceiptInfo(UUID receiptId);

	Optional<Receipt> getReceiptAndOrdersAndMenus(UUID receiptId);

	Optional<Receipt> getReceiptWithTableAndStore(UUID receiptId);

	List<Receipt> getStopReceiptsWithTableAndStore(List<UUID> receiptIds);

	List<Receipt> getStopReceiptsWithStore(List<UUID> receiptIds);

	Optional<Receipt> getNonStopReceiptsWithTableAndStoreAndLock(UUID receiptId);

	Optional<Receipt> getReceiptsWithStoreAndLock(UUID receiptId);

	List<Receipt> getNonStopReceiptsWithTableStoreAndOrdersAndLock(List<UUID> receiptIds);

	Page<ReceiptInfo> getAdjustedReceiptPageBySale(Pageable pageable, Long saleId);

	ReceiptInfo getNonAdjustReceipt(UUID tableId);

	List<Receipt> getAllNonAdjustReceiptWithTableAndOrders(Long saleId);

	Slice<Receipt> getCustomerReceiptSlice(int pageSize, UUID lastReceiptId, Long customerId);

	boolean existsReceipt(UUID receiptId);

	List<Receipt> stopReceiptsWithMenu(List<Receipt> patchReceipts);

	void restartReceipts(List<Receipt> receipts);

	void adjustReceipts(List<Receipt> receipts);

	void deleteReceipt(UUID receiptId);

	boolean isExistsNonAdjustReceiptBySaleId(Long saleId);

	Long updateReceiptTable(Receipt receipt, Table moveTable);
}
