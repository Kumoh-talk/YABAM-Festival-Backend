package com.pos.receipt.repository.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.pos.order.mapper.OrderMapper;
import com.pos.receipt.entity.ReceiptEntity;
import com.pos.receipt.mapper.ReceiptMapper;
import com.pos.receipt.repository.jpa.ReceiptJpaRepository;
import com.pos.sale.mapper.SaleMapper;
import com.pos.store.mapper.StoreMapper;
import com.pos.table.mapper.TableMapper;
import com.vo.UserPassport;

import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.receipt.port.required.ReceiptRepository;
import domain.pos.sale.entity.Sale;
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
	public Optional<ReceiptInfo> getReceiptInfo(UUID receiptId) {
		return receiptJpaRepository.findById(receiptId)
			.map(ReceiptMapper::toReceiptInfo);
	}

	@Override
	public Optional<Receipt> getReceiptAndOrdersAndMenus(UUID receiptId) {
		return receiptJpaRepository.findByIdWithOrders(receiptId)
			.map(ReceiptMapper::toReceiptWithMenus);
	}

	@Override
	public Optional<Receipt> getReceiptWithTableAndStore(UUID receiptId) {
		return receiptJpaRepository.findByIdWithTableAndStore(receiptId)
			.map(receiptEntity -> ReceiptMapper.toReceipt(receiptEntity,
				TableMapper.toTable(receiptEntity.getTable(), (Store)null),
				SaleMapper.toSale(receiptEntity.getSale(), StoreMapper.toStore(receiptEntity.getSale().getStore()))));
	}

	@Override
	public List<Receipt> getStopReceiptsWithTableAndStore(List<UUID> receiptIds) {
		return receiptJpaRepository.findStopReceiptsByIdWithTableAndStore(receiptIds)
			.stream()
			.map(receiptEntity -> ReceiptMapper.toReceipt(receiptEntity,
				TableMapper.toTable(receiptEntity.getTable(), (Store)null),
				SaleMapper.toSale(receiptEntity.getSale(), StoreMapper.toStore(receiptEntity.getSale().getStore()))))
			.collect(Collectors.toList());
	}

	@Override
	public List<Receipt> getStopReceiptsWithStore(List<UUID> receiptIds) {
		return receiptJpaRepository.findStopReceiptsByIdWithStore(receiptIds)
			.stream()
			.map(receiptEntity -> ReceiptMapper.toReceipt(receiptEntity, null,
				SaleMapper.toSale(receiptEntity.getSale(), StoreMapper.toStore(receiptEntity.getSale().getStore()))))
			.collect(Collectors.toList());
	}

	@Override
	public Optional<Receipt> getNonStopReceiptsWithTableAndStoreAndLock(UUID receiptId) {
		return receiptJpaRepository.findNonStopReceiptsByIdWithTableAndStoreAndLock(receiptId)
			.map(receiptEntity -> ReceiptMapper.toReceipt(receiptEntity,
				TableMapper.toTable(receiptEntity.getTable(), (Store)null),
				SaleMapper.toSale(receiptEntity.getSale(), StoreMapper.toStore(receiptEntity.getSale().getStore()))));
	}

	@Override
	public Optional<Receipt> getReceiptsWithStoreAndLock(UUID receiptId) {
		return receiptJpaRepository.findReceiptsByIdWithStoreAndLock(receiptId)
			.map(receiptEntity -> ReceiptMapper.toReceipt(receiptEntity, null,
				SaleMapper.toSale(receiptEntity.getSale(), StoreMapper.toStore(receiptEntity.getSale().getStore()))));
	}

	@Override
	public List<Receipt> getNonStopReceiptsWithTableStoreAndOrdersAndLock(List<UUID> receiptIds) {
		return receiptJpaRepository.findNonStopReceiptsWithTableStoreAndOrdersAndLock(receiptIds)
			.stream()
			.map(receiptEntity -> ReceiptMapper.toReceipt(receiptEntity,
				TableMapper.toTable(receiptEntity.getTable(), StoreMapper.toStore(receiptEntity.getTable().getStore())),
				null))
			.collect(Collectors.toList());
	}

	@Override
	public Page<ReceiptInfo> getAdjustedReceiptPageBySale(Pageable pageable, Long saleId) {
		return receiptJpaRepository.findAdjustedReceiptPageBySaleId(pageable, saleId)
			.map(ReceiptMapper::toReceiptInfo);
	}

	@Override
	public ReceiptInfo getNonAdjustReceipt(UUID tableId) {
		ReceiptEntity receiptEntity = receiptJpaRepository.findNonAdjustReceipt(tableId);
		return receiptEntity == null ? null : ReceiptMapper.toReceiptInfo(receiptEntity);
	}

	@Override
	public List<Receipt> getAllNonAdjustReceiptWithTableAndOrders(Long saleId) {
		return receiptJpaRepository.findAllNonAdjustReceiptWithTableAndOrders(saleId)
			.stream()
			.sorted(Comparator.comparing(
				r -> r.getTable().getTableNumber().getTableNumber()
			))
			.map(receiptEntity -> {
				Receipt receipt = ReceiptMapper.toReceipt(
					receiptEntity,
					TableMapper.toTable(receiptEntity.getTable(), (Store)null),
					null);
				receipt.getOrders().addAll(receiptEntity.getOrders().stream()
					.map(orderEntity -> OrderMapper.toOrder(orderEntity, null, null))
					.collect(Collectors.toList()));
				return receipt;
			})
			.collect(Collectors.toList());
	}

	@Override
	public Slice<Receipt> getCustomerReceiptSlice(int pageSize, UUID lastReceiptId, Long customerId) {
		return receiptJpaRepository.findCustomerReceiptSliceWithStore(pageSize, lastReceiptId, customerId)
			.map(receiptEntity -> ReceiptMapper.toReceipt(receiptEntity, null,
				SaleMapper.toSale(receiptEntity.getSale(), StoreMapper.toStore(receiptEntity.getSale().getStore()))));
	}

	@Override
	public boolean existsReceipt(UUID receiptId) {
		return receiptJpaRepository.existsById(receiptId);
	}

	@Override
	public List<Receipt> stopReceiptsWithMenu(List<Receipt> patchReceipts) {
		List<Receipt> responseReceipts = new ArrayList<>();
		for (Receipt patchReceipt : patchReceipts) {
			ReceiptEntity receiptEntity = receiptJpaRepository.findByIdWithOrders(
				patchReceipt.getReceiptInfo().getReceiptId()).get();
			receiptEntity.updateInfo(patchReceipt.getReceiptInfo());

			Receipt receipt = ReceiptMapper.toReceiptWithMenus(receiptEntity);
			receipt.filterCompletedOrders();
			responseReceipts.add(receipt);
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
	public void deleteReceipt(UUID receiptId) {
		ReceiptEntity receiptEntity = receiptJpaRepository.findById(receiptId).get();
		receiptJpaRepository.delete(receiptEntity);
	}

	@Override
	public boolean isExistsNonAdjustReceiptBySaleId(Long saleId) {
		return receiptJpaRepository.existsNonAdjustReceiptBySaleId(saleId);
	}

	@Override
	public Long updateReceiptTable(Receipt receipt, Table moveTable) {
		return receiptJpaRepository.updateReceiptTable(receipt.getReceiptInfo().getReceiptId(),
			moveTable.getId());
	}

	@Override
	public Optional<Receipt> getReceiptById(UUID receiptId) {
		return Optional.empty();
	}

	@Override
	public List<domain.pos.receipt.entity.v2.domain.Receipt> writeLock(List<UUID> receiptIds) {
		return null;
	}

	@Override
	public Optional<domain.pos.receipt.entity.v2.domain.Receipt> readLock(UUID receiptId) {
		return Optional.empty();
	}

	@Override
	public Optional<domain.pos.receipt.entity.v2.domain.Receipt> create(UserPassport userPassport, Long storeId,
		domain.pos.receipt.entity.v2.domain.Receipt receipt) {
		return Optional.empty();
	}

	@Override
	public Optional<domain.pos.receipt.entity.v2.domain.Receipt> readReceipt(UUID receiptId) {
		return Optional.empty();
	}

	@Override
	public List<domain.pos.receipt.entity.v2.domain.Receipt> readNonAdjusts(UserPassport userPassport, Long saleId) {
		return null;
	}

	@Override
	public Optional<domain.pos.receipt.entity.v2.domain.Receipt> readNonAdjusts(UUID tableId) {
		return Optional.empty();
	}

	@Override
	public Page<domain.pos.receipt.entity.v2.domain.Receipt> readAdjustedPage(UserPassport userPassport,
		Pageable pageable, Long saleId) {
		return null;
	}

	@Override
	public int bulkUpdateStartUsageTime(UserPassport userPassport, List<UUID> receiptIds,
		LocalDateTime startUsageTime) {
		return 0;
	}

	@Override
	public int bulkUpdateStopUsageTime(UserPassport userPassport,
		List<domain.pos.receipt.entity.v2.domain.Receipt> receipts) {
		return 0;
	}

	@Override
	public int bulkUpdateRestartUsage(UserPassport userPassport, List<UUID> receiptIds) {
		return 0;
	}

	@Override
	public int bulkUpdateAdjust(UserPassport userPassport, List<UUID> receiptIds) {
		return 0;
	}

	@Override
	public int updateTableId(UserPassport userPassport, UUID receiptId, UUID tableId) {
		return 0;
	}

	@Override
	public Optional<Object> delete(UserPassport userPassport, UUID receiptId) {
		return Optional.empty();
	}
}
