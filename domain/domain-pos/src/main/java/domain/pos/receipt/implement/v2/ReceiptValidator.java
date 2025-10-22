package domain.pos.receipt.implement.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import domain.pos.order.entity.vo.OrderStatus;
import domain.pos.order.repository.OrderRepository;
import domain.pos.receipt.entity.v2.domain.Receipt;
import domain.pos.receipt.entity.v2.dto.CreateValidationResult;
import domain.pos.receipt.entity.v2.dto.ReceiptOrderStatus;
import domain.pos.receipt.port.required.ReceiptRepository;
import domain.pos.sale.entity.Sale;
import domain.pos.sale.port.required.SaleRepository;
import domain.pos.store.implement.StoreValidator;
import domain.pos.table.entity.Table;
import domain.pos.table.port.required.repository.TableRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReceiptValidator {
	private final SaleRepository saleRepository;
	private final TableRepository tableRepository;
	private final ReceiptRepository receiptRepository;
	private final OrderRepository orderRepository;

	private final StoreValidator storeValidator;

	public CreateValidationResult validateForCreate(UserPassport userPassport, Long storeId, UUID tableId) {
		storeValidator.validateStoreOwner(userPassport, storeId);
		Sale sale = validateSaleOpen(storeId);
		Table table = validateTableActive(tableId);
		validateTableStoreId(table, storeId);

		return new CreateValidationResult(sale, table);
	}

	public List<Receipt> validateForStopUsage(UserPassport userPassport, List<UUID> receiptIds) {
		validateAllStoreOwners(userPassport, receiptIds);
		List<Receipt> receipts = validateAlreadyStopped(receiptIds);
		validateCompleteOrders(receiptIds);

		return receipts;
	}

	public void validateForMoveTable(UserPassport userPassport, UUID receiptId, UUID moveTableId) {
		validateSingleStoreOwner(userPassport, receiptId);
		validateTableActive(moveTableId);
	}

	public void validateForSyncStart(UserPassport userPassport, UUID baseReceiptId,
		List<UUID> receiptIds) {
		List<UUID> allReceiptIds = new ArrayList<>(receiptIds);
		allReceiptIds.add(baseReceiptId);

		validateAllStoreOwners(userPassport, allReceiptIds);
	}

	public Sale validateSaleOpen(Long saleId) {
		Sale sale = saleRepository.readLock(saleId)
			.orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND_SALE));

		if (sale.getCloseDateTime().isPresent()) {
			throw new ServiceException(ErrorCode.CLOSE_SALE);
		}
		return sale;
	}

	public Table validateTableActive(UUID tableId) {
		Table table = tableRepository.writeLock(tableId)
			.orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND_TABLE));
		if (table.getIsActive()) {
			throw new ServiceException(ErrorCode.ALREADY_ACTIVE_TABLE);
		}
		return table;
	}

	public void validateTableStoreId(Table table, Long storeId) {
		if (!table.getStoreId().equals(storeId)) {
			throw new ServiceException(ErrorCode.NOT_FOUND_TABLE);
		}
	}

	public List<Receipt> validateAlreadyStopped(List<UUID> receiptIds) {
		List<Receipt> receipts = receiptRepository.writeLock(receiptIds);
		if (receipts.size() != receiptIds.size()) {
			throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
		}
		for (Receipt receipt : receipts) {
			if (receipt.getUsageTime().getStop() != null) {
				throw new ServiceException(ErrorCode.ALREADY_STOPPED_RECEIPT);
			}
		}
		return receipts;
	}

	public List<ReceiptOrderStatus> validateCompleteOrders(List<UUID> receiptIds) {
		List<ReceiptOrderStatus> receiptOrderStatuses = orderRepository.readLock(receiptIds);
		for (ReceiptOrderStatus r : receiptOrderStatuses) {
			if (r.getOrderStatus() != OrderStatus.COMPLETED) {
				throw new ServiceException(ErrorCode.ORDER_NOT_COMPLETED, Map.of("receiptId", r.getReceiptId()));
			}
		}
		return receiptOrderStatuses;
	}

	public void validateAllStoreOwners(UserPassport userPassport, List<UUID> receiptIds) {
		if (receiptRepository.validateStoreOwner(userPassport, receiptIds) != receiptIds.size()) {
			throw new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER);
		}
	}

	public void validateSingleStoreOwner(UserPassport userPassport, UUID receiptId) {
		if (receiptRepository.validateStoreOwner(userPassport, receiptId) != 1) {
			throw new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER);
		}
	}

	public void validateSaleStoreOwner(UserPassport userPassport, Long saleId) {
		Sale sale = saleRepository.findSaleWithStoreBySaleId(saleId)
			.orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND_SALE));

		if (!sale.getStore().getOwnerPassport().getUserId().equals(userPassport.getUserId())) {
			throw new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER);
		}
	}
}
