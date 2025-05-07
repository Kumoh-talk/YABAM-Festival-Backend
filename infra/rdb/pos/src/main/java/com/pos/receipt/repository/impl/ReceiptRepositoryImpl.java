package com.pos.receipt.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.pos.receipt.entity.ReceiptEntity;
import com.pos.receipt.mapper.ReceiptMapper;
import com.pos.receipt.repository.jpa.ReceiptJpaRepository;
import com.pos.sale.mapper.SaleMapper;
import com.pos.store.mapper.StoreMapper;
import com.pos.table.mapper.TableMapper;

import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.receipt.repository.ReceiptRepository;
import domain.pos.store.entity.Sale;
import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReceiptRepositoryImpl implements ReceiptRepository {
	private final ReceiptJpaRepository receiptJpaRepository;

	@Override
	public Receipt createReceipt(Table table, Sale sale) {
		ReceiptEntity receiptEntity = ReceiptMapper.toReceiptEntity(sale, table);
		return ReceiptMapper.toReceipt(receiptJpaRepository.save(receiptEntity), table, sale);
	}

	@Override
	public Optional<ReceiptInfo> getReceiptInfo(Long receiptId) {
		return receiptJpaRepository.findById(receiptId)
			.map(ReceiptMapper::toReceiptInfo);
	}

	@Override
	public Optional<Receipt> getReceiptWithTableAndStore(Long receiptId) {
		return receiptJpaRepository.findByIdWithTableAndStore(receiptId)
			.map(receiptEntity -> ReceiptMapper.toReceipt(receiptEntity,
				TableMapper.toTable(receiptEntity.getTable(), (Store)null),
				SaleMapper.toSale(receiptEntity.getSale(), StoreMapper.toStore(receiptEntity.getSale().getStore()))));
	}

	@Override
	public List<Receipt> getStopReceiptsWithTableAndStore(List<Long> receiptIds) {
		return receiptJpaRepository.findStopReceiptsByIdWithTableAndStore(receiptIds)
			.stream()
			.map(receiptEntity -> ReceiptMapper.toReceipt(receiptEntity,
				TableMapper.toTable(receiptEntity.getTable(), (Store)null),
				SaleMapper.toSale(receiptEntity.getSale(), StoreMapper.toStore(receiptEntity.getSale().getStore()))))
			.collect(Collectors.toList());
	}

	@Override
	public List<Receipt> getStopReceiptsWithStore(List<Long> receiptIds) {
		return receiptJpaRepository.findStopReceiptsByIdWithStore(receiptIds)
			.stream()
			.map(receiptEntity -> ReceiptMapper.toReceipt(receiptEntity, null,
				SaleMapper.toSale(receiptEntity.getSale(), StoreMapper.toStore(receiptEntity.getSale().getStore()))))
			.collect(Collectors.toList());
	}

	@Override
	public Optional<Receipt> getNonStopReceiptsWithTableAndStoreAndLock(Long receiptId) {
		return receiptJpaRepository.findNonStopReceiptsByIdWithTableAndStoreAndLock(receiptId)
			.map(receiptEntity -> ReceiptMapper.toReceipt(receiptEntity,
				TableMapper.toTable(receiptEntity.getTable(), (Store)null),
				SaleMapper.toSale(receiptEntity.getSale(), StoreMapper.toStore(receiptEntity.getSale().getStore()))));
	}

	@Override
	public List<Receipt> getNonStopReceiptsWithStoreAndLock(List<Long> receiptIds) {
		return receiptJpaRepository.findNonStopReceiptsByIdWithStoreAndLock(receiptIds)
			.stream()
			.map(receiptEntity -> ReceiptMapper.toReceipt(receiptEntity, null,
				SaleMapper.toSale(receiptEntity.getSale(), StoreMapper.toStore(receiptEntity.getSale().getStore()))))
			.collect(Collectors.toList());
	}

	@Override
	public Page<ReceiptInfo> getAdjustedReceiptPageBySale(Pageable pageable, Long saleId) {
		return receiptJpaRepository.findAdjustedReceiptPageBySaleId(pageable, saleId)
			.map(ReceiptMapper::toReceiptInfo);
	}

	@Override
	public ReceiptInfo getNonAdjustReceipt(Long tableId) {
		return ReceiptMapper.toReceiptInfo(receiptJpaRepository.findNonAdjustReceipt(tableId));
	}

	@Override
	public Slice<Receipt> getCustomerReceiptSlice(int pageSize, Long lastReceiptId, Long customerId) {
		return receiptJpaRepository.findCustomerReceiptSliceWithStore(pageSize, lastReceiptId, customerId)
			.map(receiptEntity -> ReceiptMapper.toReceipt(receiptEntity, null,
				SaleMapper.toSale(receiptEntity.getSale(), StoreMapper.toStore(receiptEntity.getSale().getStore()))));
	}

	@Override
	public boolean existsReceipt(Long receiptId) {
		return receiptJpaRepository.existsById(receiptId);
	}

	@Override
	public List<Receipt> stopReceiptsWithMenu(List<Receipt> patchReceipts) {
		List<Receipt> responseReceipts = new ArrayList<>();
		for (Receipt patchReceipt : patchReceipts) {
			ReceiptEntity receiptEntity = receiptJpaRepository.findByIdWithOrders(
				patchReceipt.getReceiptInfo().getReceiptId()).get();
			receiptEntity.updateInfo(patchReceipt.getReceiptInfo());

			responseReceipts.add(ReceiptMapper.toReceiptWithMenus(receiptEntity));
		}
		return responseReceipts;
	}

	@Override
	public void restartReceipts(List<Receipt> receipts) {
		receiptJpaRepository.restartReceipts(receipts);
	}

	@Override
	public void adjustReceipts(List<Receipt> receipts) {
		receiptJpaRepository.adjustReceipts(receipts);
	}

	@Override
	public void deleteReceipt(Long receiptId) {
		ReceiptEntity receiptEntity = receiptJpaRepository.findById(receiptId).get();
		receiptJpaRepository.delete(receiptEntity);
	}
}
