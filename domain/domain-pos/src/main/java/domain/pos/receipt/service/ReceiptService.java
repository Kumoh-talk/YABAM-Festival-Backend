package domain.pos.receipt.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import domain.pos.member.implement.UserPassportValidator;
import domain.pos.order.entity.Order;
import domain.pos.order.entity.vo.OrderStatus;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.receipt.entity.TableWithNonAdjustReceipt;
import domain.pos.receipt.implement.ReceiptReader;
import domain.pos.receipt.implement.ReceiptValidator;
import domain.pos.receipt.implement.ReceiptWriter;
import domain.pos.sale.entity.Sale;
import domain.pos.store.entity.Store;
import domain.pos.store.implement.SaleReader;
import domain.pos.store.implement.StoreValidator;
import domain.pos.table.entity.Table;
import domain.pos.table.implement.TableReader;
import domain.pos.table.implement.TableWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReceiptService {
	private final ReceiptValidator receiptValidator;
	private final StoreValidator storeValidator;
	private final UserPassportValidator userPassportValidator;

	private final SaleReader saleReader;
	private final TableWriter tableWriter;
	private final TableReader tableReader;
	private final ReceiptWriter receiptWriter;
	private final ReceiptReader receiptReader;

	// TODO : 이것도 SSE로 전송해야하지 않을까..?
	@Transactional
	public Receipt registerReceipt(final Long storeId, final UUID tableId) {
		final Sale savedSale = saleReader.getOpenSaleByStoreId(storeId)
			.orElseThrow(() -> {
				log.warn("Open된 Sale 을 찾을 수 없습니다. tableId: {}", tableId);
				throw new ServiceException(ErrorCode.NOT_FOUND_SALE);
			});

		final Table savedTable = tableReader.findLockTableById(tableId, storeId)
			.orElseThrow(() -> {
				log.warn("Table 을 찾을 수 없습니다. tableId: {}", tableId);
				throw new ServiceException(ErrorCode.NOT_FOUND_TABLE);
			});

		if (savedTable.getIsActive()) {
			log.warn("Table 이 이미 활성화 되어 있습니다. tableId: {}", tableId);
			throw new ServiceException(ErrorCode.ALREADY_ACTIVE_TABLE);
		}

		final Table changedActiveTable = tableWriter.changeTableActiveStatus(true, savedTable);
		return receiptWriter.createReceipt(changedActiveTable, savedSale);
	}

	@Transactional
	public Receipt getReceiptAndOrdersAndMenus(UUID receiptId) {
		return receiptReader.getReceiptAndOrdersAndMenus(receiptId)
			.orElseThrow(() -> {
				log.warn("Receipt 을 찾을 수 없습니다. receiptId: {}", receiptId);
				return new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
			});
	}

	public List<TableWithNonAdjustReceipt> getAllTableNonAdjustReceipts(UserPassport ownerPassport, Long saleId) {
		Sale sale = saleReader.readSingleSale(saleId)
			.orElseThrow(() -> {
				log.warn("Sale 을 찾을 수 없습니다. saleId: {}", saleId);
				return new ServiceException(ErrorCode.NOT_FOUND_SALE);
			});

		storeValidator.validateStoreOwner(ownerPassport, sale.getStore());

		List<Table> tables = new ArrayList<>(tableReader.findTables(sale.getStore().getId()));
		tables.sort(Comparator.comparingInt((table) -> table.getTableNumber().value()));
		List<Receipt> receipts = receiptReader.getAllNonAdjustReceiptWithTableAndOrders(saleId);

		return tables.stream()
			.map(table -> TableWithNonAdjustReceipt.of(table, receipts))
			.toList();
	}

	// Owner api
	public Page<ReceiptInfo> getAdjustedReceiptPageBySale(Pageable pageable, UserPassport userPassport, Long saleId) {
		Sale sale = saleReader.readSingleSale(saleId)
			.orElseThrow(() -> {
				log.warn("Sale 을 찾을 수 없습니다. saleId: {}, userId: {}", saleId, null);
				return new ServiceException(ErrorCode.NOT_FOUND_SALE);
			});

		storeValidator.validateStoreOwner(userPassport, sale.getStore());

		return receiptReader.getAdjustedReceiptPageBySale(pageable, saleId);
	}

	@Transactional
	public List<Receipt> stopReceiptUsage(List<UUID> receiptIds, UserPassport userPassport) {
		List<Receipt> receipts = receiptReader.getNonStopReceiptsWithTableStoreAndOrdersAndLock(receiptIds);
		if (receipts.size() != receiptIds.size()) {
			log.warn("Receipt 을 찾을 수 없습니다.");
			throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
		}

		LocalDateTime stopUsageTime = LocalDateTime.now();
		int maxUnitCount = 0;
		for (Receipt receipt : receipts) {
			storeValidator.validateStoreOwner(userPassport, receipt.getTable().getStore());
			for (Order order : receipt.getOrders()) {
				if (order.getOrderStatus() != OrderStatus.COMPLETED) {
					log.warn("모든 주문이 완료되지 않았습니다. orderId: {}", order.getOrderId());
					throw new ServiceException(ErrorCode.ORDER_NOT_COMPLETED);
				}
			}
			maxUnitCount = Math.max(receipt.getReceiptInfo().calculateUnitCount(stopUsageTime), maxUnitCount);
		}
		for (Receipt receipt : receipts) {
			receipt.getReceiptInfo().stopUsage(receipt.getTable(), stopUsageTime, maxUnitCount);
		}

		return receiptWriter.stopReceiptsWithMenu(receipts);
	}

	@Transactional
	public void restartReceiptUsage(List<UUID> receiptIds, UserPassport userPassport) {
		List<Receipt> receipts = receiptReader.getStopReceiptsWithStore(receiptIds);
		if (receipts.size() != receiptIds.size()) {
			log.warn("Receipt 을 찾을 수 없습니다.");
			throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
		}
		for (Receipt receipt : receipts) {
			receiptValidator.validateIsOwner(receipt, userPassport);
		}

		receiptWriter.restartReceipts(receipts);
	}

	// Owner api
	@Transactional
	public void adjustReceipts(List<UUID> receiptIds, UserPassport userPassport) {
		List<Receipt> receipts = receiptReader.getStopReceiptsWithTableAndStore(receiptIds);
		if (receipts.size() != receiptIds.size()) {
			log.warn("Receipt 을 찾을 수 없습니다.");
			throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
		}

		for (Receipt receipt : receipts) {
			receiptValidator.validateIsOwner(receipt, userPassport);
			if (receipt.getReceiptInfo().isAdjustment()) {
				log.warn("이미 정산된 영수증입니다. receiptId: {}", receipt.getReceiptInfo().getReceiptId());
				throw new ServiceException(ErrorCode.ALREADY_ADJUSTMENT_RECEIPT);
			}
			tableWriter.changeTableActiveStatus(false, receipt.getTable());
		}

		receiptWriter.adjustReceipts(receipts);
	}

	// Owner api
	@Transactional
	public void deleteReceipt(UUID receiptId, UserPassport userPassport) {
		Receipt receipt = receiptReader.getReceiptWithTableAndStore(receiptId)
			.orElseThrow(() -> {
				log.warn("Receipt 을 찾을 수 없습니다. receiptId: {}", receiptId);
				return new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
			});
		receiptValidator.validateIsOwner(receipt, userPassport);
		if (!receipt.getReceiptInfo().isAdjustment()) {
			tableWriter.changeTableActiveStatus(false, receipt.getTable());
		}
		receiptWriter.deleteReceipt(receiptId);
	}

	public UUID getNonAdjustReceiptId(UUID tableId) {
		ReceiptInfo receiptInfo = receiptReader.getNonAdjustReceipt(tableId);
		return receiptInfo != null ? receiptInfo.getReceiptId() : null;
	}

	public Slice<Receipt> getCustomerReceiptSlice(int pageSize, UserPassport userPassport, Long customerId,
		UUID lastReceiptId) {
		userPassportValidator.validateUserPassport(userPassport, customerId);
		if (lastReceiptId != null) {
			if (!receiptReader.existsReceipt(lastReceiptId)) {
				log.warn("lastReceipt 을 찾을 수 없습니다. receiptId: {}", lastReceiptId);
				throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
			}
		}
		return receiptReader.getCustomerReceiptSlice(pageSize, lastReceiptId, customerId);
	}

	//TODO : 코드 리팩터링 필요 너무 대충 짬
	@Transactional
	public void moveReceiptTable(
		final UserPassport ownerPassport,
		final UUID receiptId,
		final UUID moveTableId
	) {
		final Receipt receipt = receiptReader.getReceiptWithTableAndStore(receiptId)
			.orElseThrow(() -> {
				log.warn("Receipt 을 찾을 수 없습니다. receiptId: {}", receiptId);
				return new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
			});
		Table table = receipt.getTable();
		Store store = receipt.getSale().getStore();
		if (!store.getOwnerPassport().getUserId().equals(ownerPassport.getUserId())) {
			log.warn("Store 의 Owner 가 아닙니다. storeId: {}, userId: {}", store.getId(), ownerPassport.getUserId());
			throw new ServiceException(ErrorCode.NOT_VALID_OWNER);
		}
		Table moveTable = tableReader.findLockTableById(moveTableId, store.getId())
			.orElseThrow(() -> {
				log.warn("Table 을 찾을 수 없습니다. tableId: {}", moveTableId);
				throw new ServiceException(ErrorCode.NOT_FOUND_TABLE);
			});
		if (moveTable.getIsActive()) {
			log.warn("Table 이 이미 활성화 되어 있습니다. tableId: {}", moveTableId);
			throw new ServiceException(ErrorCode.ALREADY_ACTIVE_TABLE);
		}
		receiptWriter.moveReceiptTable(receipt, moveTable);
		tableWriter.changeTableActiveStatus(true, moveTable);
		tableWriter.changeTableActiveStatus(false, table);
	}

}
