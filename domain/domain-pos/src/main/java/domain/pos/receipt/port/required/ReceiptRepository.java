package domain.pos.receipt.port.required;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.vo.UserPassport;

import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.sale.entity.Sale;
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

	Optional<Receipt> getReceiptById(UUID receiptId);

	// v2
	Optional<domain.pos.receipt.entity.v2.domain.Receipt> writeLock(UUID receiptIds);

	List<domain.pos.receipt.entity.v2.domain.Receipt> writeLock(List<UUID> receiptIds);

	Optional<domain.pos.receipt.entity.v2.domain.Receipt> readLock(UUID receiptId);

	Optional<domain.pos.receipt.entity.v2.domain.Receipt> create(UserPassport userPassport, Long storeId,
		domain.pos.receipt.entity.v2.domain.Receipt receipt);

	Optional<domain.pos.receipt.entity.v2.domain.Receipt> readReceipt(UUID receiptId);

	List<domain.pos.receipt.entity.v2.domain.Receipt> readNonAdjusts(UserPassport userPassport, Long saleId);

	Optional<domain.pos.receipt.entity.v2.domain.Receipt> readNonAdjusts(UUID tableId);

	Page<domain.pos.receipt.entity.v2.domain.Receipt> readAdjustedPage(UserPassport userPassport, Pageable pageable,
		Long saleId);

	int bulkUpdateStartUsageTime(UserPassport userPassport, List<UUID> receiptIds, LocalDateTime startUsageTime);

	int bulkUpdateStopUsageTime(UserPassport userPassport, List<domain.pos.receipt.entity.v2.domain.Receipt> receipts);

	int bulkUpdateRestartUsage(UserPassport userPassport, List<UUID> receiptIds);

	int bulkUpdateAdjust(UserPassport userPassport, List<UUID> receiptIds);

	int updateTableId(UserPassport userPassport, UUID receiptId, UUID tableId);

	Optional<Object> delete(UserPassport userPassport, UUID receiptId);
}
