package domain.pos.receipt.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserPassport;
import domain.pos.member.implement.UserPassportValidator;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.receipt.implement.ReceiptReader;
import domain.pos.receipt.implement.ReceiptValidator;
import domain.pos.receipt.implement.ReceiptWriter;
import domain.pos.store.entity.Sale;
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
	public Receipt registerReceipt(final Long queryTableId, final Long querySaleId) {
		final Sale savedSale = saleReader.readSingleSale(querySaleId)
			.orElseThrow(() -> {
				log.warn("Sale 을 찾을 수 없습니다. saleId: {}", querySaleId);
				throw new ServiceException(ErrorCode.NOT_FOUND_SALE);
			});

		final Table savedTable = tableReader.findLockTableById(queryTableId, savedSale.getStore().getStoreId())
			.orElseThrow(() -> {
				log.warn("Table 을 찾을 수 없습니다. tableId: {}", queryTableId);
				throw new ServiceException(ErrorCode.NOT_FOUND_TABLE);
			});

		if (savedTable.getIsActive()) {
			log.warn("Table 이 이미 활성화 되어 있습니다. tableId: {}", queryTableId);
			throw new ServiceException(ErrorCode.ALREADY_ACTIVE_TABLE);
		}

		final Table changedActiveTable = tableWriter.changeTableActiveStatus(true, savedTable);
		return receiptWriter.createReceipt(changedActiveTable, savedSale);
	}

	public ReceiptInfo getReceiptInfo(Long receiptId) {
		return receiptReader.getReceiptInfo(receiptId)
			.orElseThrow(() -> {
				log.warn("Receipt 을 찾을 수 없습니다. receiptId: {}", receiptId);
				return new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
			});
	}

	// Owner api
	public Page<ReceiptInfo> getAdjustedReceiptPageBySale(Pageable pageable, UserPassport userPassport, Long saleId) {
		Sale sale = saleReader.readSingleSale(saleId)
			.orElseThrow(() -> {
				log.warn("Sale 을 찾을 수 없습니다. saleId: {}, userId: {}", saleId, null);
				return new ServiceException(ErrorCode.NOT_FOUND_SALE);
			});

		storeValidator.validateStoreOwner(userPassport, sale.getStore().getStoreId());

		return receiptReader.getAdjustedReceiptPageBySale(pageable, saleId);
	}

	@Transactional
	public List<Receipt> stopReceiptUsage(List<Long> receiptIds, UserPassport userPassport) {
		List<Receipt> receipts = receiptReader.getNonStopReceiptsWithStoreAndLock(receiptIds);
		if (receipts.size() != receiptIds.size()) {
			log.warn("Receipt 을 찾을 수 없습니다.");
			throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
		}
		for (Receipt receipt : receipts) {
			receiptValidator.validateIsOwner(receipt, userPassport);
			receipt.getReceiptInfo().stop(receipt.getSale().getStore());
		}

		return receiptWriter.stopReceiptsWithMenu(receipts);
	}

	@Transactional
	public void restartReceiptUsage(List<Long> receiptIds, UserPassport userPassport) {
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
	// TODO : 정산이 끝나면 주문창 세션을 종료시켜야함
	@Transactional
	public void adjustReceipts(List<Long> receiptIds, UserPassport userPassport) {
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
	public void deleteReceipt(Long receiptId, UserPassport userPassport) {
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

	public Long getNonAdjustReceiptId(Long tableId) {
		ReceiptInfo receiptInfo = receiptReader.getNonAdjustReceipt(tableId);
		return receiptInfo != null ? receiptInfo.getReceiptId() : null;
	}

	public Slice<Receipt> getCustomerReceiptSlice(int pageSize, UserPassport userPassport, Long customerId,
		Long lastReceiptId) {
		userPassportValidator.validateUserPassport(userPassport, customerId);
		if (lastReceiptId != null) {
			if (!receiptReader.existsReceipt(lastReceiptId)) {
				log.warn("lastReceipt 을 찾을 수 없습니다. receiptId: {}", lastReceiptId);
				throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
			}
		}
		return receiptReader.getCustomerReceiptSlice(pageSize, lastReceiptId, customerId);
	}

}
