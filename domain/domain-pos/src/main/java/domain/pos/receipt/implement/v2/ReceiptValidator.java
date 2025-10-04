package domain.pos.receipt.implement.v2;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.order.entity.vo.OrderStatus;
import domain.pos.order.repository.OrderRepository;
import domain.pos.receipt.entity.v2.domain.Receipt;
import domain.pos.receipt.entity.v2.dto.ReceiptOrderStatus;
import domain.pos.receipt.port.required.ReceiptRepository;
import domain.pos.sale.entity.Sale;
import domain.pos.sale.port.required.SaleRepository;
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

	public List<Receipt> validateForStopUsage(List<UUID> receiptIds) {
		List<Receipt> receipts = receiptRepository.writeLock(receiptIds);
		if (receipts.size() != receiptIds.size()) {
			throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
		}
		for (Receipt receipt : receipts) {
			if (receipt.getUsageTime().getStop() != null) {
				throw new ServiceException(ErrorCode.ALREADY_STOPPED_RECEIPT);
			}
		}

		List<ReceiptOrderStatus> receiptOrderStatuses = orderRepository.readLock(receiptIds);
		for (ReceiptOrderStatus r : receiptOrderStatuses) {
			if (r.getOrderStatus() != OrderStatus.COMPLETED) {
				throw new ServiceException(ErrorCode.ORDER_NOT_COMPLETED, Map.of("receiptId", r.getReceiptId()));
			}
		}
		return receipts;
	}

	public Sale validateSaleOpen(Long saleId) {
		Sale sale = saleRepository.readLock(saleId)
			.orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND_SALE));

		if (sale.getCloseDateTime().isEmpty()) {
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
}
