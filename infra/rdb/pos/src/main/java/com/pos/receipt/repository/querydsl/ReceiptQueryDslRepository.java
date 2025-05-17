package com.pos.receipt.repository.querydsl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.pos.receipt.entity.ReceiptEntity;

import domain.pos.receipt.entity.Receipt;

public interface ReceiptQueryDslRepository {
	Optional<ReceiptEntity> findByIdWithTableAndStore(UUID receiptId);

	List<ReceiptEntity> findStopReceiptsByIdWithTableAndStore(List<UUID> receiptIds);

	List<ReceiptEntity> findStopReceiptsByIdWithStore(List<UUID> receiptIds);

	Optional<ReceiptEntity> findNonStopReceiptsByIdWithTableAndStoreAndLock(UUID receiptId);

	List<ReceiptEntity> findNonStopReceiptsWithTableStoreAndOrdersAndLock(List<UUID> receiptIds);

	Page<ReceiptEntity> findAdjustedReceiptPageBySaleId(Pageable pageable, Long saleId);

	ReceiptEntity findNonAdjustReceipt(UUID tableId);

	List<ReceiptEntity> findAllNonAdjustReceiptWithTableAndOrders(Long saleId);

	Slice<ReceiptEntity> findCustomerReceiptSliceWithStore(int pageSize, UUID lastReceiptId, Long customerId);

	Optional<ReceiptEntity> findByIdWithOrders(UUID receiptId);

	void restartReceipts(List<Receipt> receipts);

	void adjustReceipts(List<Receipt> receipts);

	LocalDateTime startReceiptUsage(UUID receiptId);

}
