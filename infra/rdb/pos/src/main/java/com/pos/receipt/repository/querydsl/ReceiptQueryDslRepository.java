package com.pos.receipt.repository.querydsl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.pos.receipt.entity.ReceiptEntity;

import domain.pos.receipt.entity.Receipt;

public interface ReceiptQueryDslRepository {
	Optional<ReceiptEntity> findByIdWithTableAndStore(Long receiptId);

	List<ReceiptEntity> findStopReceiptsByIdWithTableAndStore(List<Long> receiptIds);

	List<ReceiptEntity> findStopReceiptsByIdWithStore(List<Long> receiptIds);

	Optional<ReceiptEntity> findNonStopReceiptsByIdWithTableAndStoreAndLock(Long receiptId);

	List<ReceiptEntity> findNonStopReceiptsByIdWithStoreAndLock(List<Long> receiptIds);

	Page<ReceiptEntity> findAdjustedReceiptPageBySaleId(Pageable pageable, Long saleId);

	ReceiptEntity findNonAdjustReceipt(Long tableId);

	Slice<ReceiptEntity> findCustomerReceiptSliceWithStore(int pageSize, Long lastReceiptId, Long customerId);

	Optional<ReceiptEntity> findByIdWithOrders(Long receiptId);

	void restartReceipts(List<Receipt> receipts);

	void adjustReceipts(List<Receipt> receipts);

	void startReceiptUsage(Long receiptId);

}
