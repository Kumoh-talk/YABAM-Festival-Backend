package domain.pos.receipt.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserPassport;
import domain.pos.member.entity.UserRole;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.receipt.implement.ReceiptReader;
import domain.pos.receipt.implement.ReceiptWriter;
import domain.pos.store.entity.Sale;
import domain.pos.store.implement.SaleReader;
import domain.pos.table.entity.Table;
import domain.pos.table.implement.TableReader;
import domain.pos.table.implement.TableWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReceiptService {
	private final SaleReader saleReader;
	private final TableWriter tableWriter;
	private final TableReader tableReader;
	private final ReceiptWriter receiptWriter;
	private final ReceiptReader receiptReader;

	@Transactional
	public Receipt registerReceipt(final UserPassport queryUserPassport, final Long queryTableId,
		final Long querySaleId) {
		final Sale savedSale = saleReader.readSingleSale(querySaleId)
			.orElseThrow(() -> {
				log.warn("Sale 을 찾을 수 없습니다. saleId: {}, userId: {}", querySaleId, queryUserPassport.getUserId());
				throw new ServiceException(ErrorCode.NOT_FOUND_SALE);
			});

		final Table savedTable = tableReader.findLockTableById(queryTableId)
			.orElseThrow(() -> {
				log.warn("Table 을 찾을 수 없습니다. tableId: {}, userId: {}", queryTableId, queryUserPassport.getUserId());
				throw new ServiceException(ErrorCode.NOT_FOUND_TABLE);
			});

		if (savedTable.getIsActive()) {
			log.warn("Table 이 이미 활성화 되어 있습니다. tableId: {}, userId: {}", queryTableId, queryUserPassport.getUserId());
			throw new ServiceException(ErrorCode.ALREADY_ACTIVE_TABLE);
		}

		final Table changedActiveTable = tableWriter.changeTableActiveStatus(true, savedTable);
		final Receipt createdReceipt = receiptWriter.createReceipt(queryUserPassport, changedActiveTable, savedSale);
		return createdReceipt;
	}

	public ReceiptInfo getReceiptInfo(Long receiptId, UserPassport userPassport) {
		Receipt receipt = receiptReader.getReceiptWithCustomerAndOwner(receiptId)
			.orElseThrow(() -> {
				log.warn("Receipt 을 찾을 수 없습니다. receiptId: {}", receiptId);
				return new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
			});

		if (!hasAccessToReceipt(receipt, userPassport)) {
			log.warn("Receipt 접근 가능 요청자가 아닙니다. userId: {}", userPassport.getUserId());
			throw new ServiceException(ErrorCode.RECEIPT_ACCESS_DENIED);
		}

		return receipt.getReceiptInfo();
	}

	private boolean hasAccessToReceipt(Receipt receipt, UserPassport userPassport) {
		Long customerId = receipt.getUserPassport().getUserId();
		Long storeOwnerId = receipt.getSale().getStore().getOwnerPassport().getUserId();
		Long userId = userPassport.getUserId();

		return (customerId.equals(userId) && userPassport.getUserRole().equals(UserRole.ROLE_USER))
			||
			(storeOwnerId.equals(userId) && userPassport.getUserRole().equals(UserRole.ROLE_OWNER));
	}

	// TODO : application 계층 one-indexed-parameters 설정 추가
	public Page<Receipt> getReceiptPageBySale(Pageable pageable, UserPassport userPassport, Long saleId) {
		Sale sale = saleReader.readSaleWithOwner(saleId)
			.orElseThrow(() -> {
				log.warn("Sale 을 찾을 수 없습니다. saleId: {}, userId: {}", saleId, null);
				return new ServiceException(ErrorCode.NOT_FOUND_SALE);
			});

		Long storeOwnerId = sale.getStore().getOwnerPassport().getUserId();
		if (!storeOwnerId.equals(userPassport.getUserId())) {
			log.warn("Store 의 소유자와 요청자가 다릅니다. ownerId: {}, userId: {}", storeOwnerId, userPassport.getUserId());
			throw new ServiceException(ErrorCode.NOT_VALID_OWNER);
		}

		return receiptReader.getReceiptPageBySale(pageable, saleId);
	}

	public void adjustReceipt(Long receiptId, UserPassport userPassport) {
		Receipt receipt = receiptReader.getReceiptWithOwner(receiptId)
			.orElseThrow(() -> {
				log.warn("Receipt 을 찾을 수 없습니다. receiptId: {}", receiptId);
				return new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
			});

		Long storeOwnerId = receipt.getSale().getStore().getOwnerPassport().getUserId();
		if (!storeOwnerId.equals(userPassport.getUserId())) {
			log.warn("Store 의 소유자와 요청자가 다릅니다. ownerId: {}, userId: {}", storeOwnerId, userPassport.getUserId());
			throw new ServiceException(ErrorCode.NOT_VALID_OWNER);
		}
		if (receipt.getReceiptInfo().isAdjustment()) {
			log.warn("이미 정산된 영수증입니다. receiptId: {}", receiptId);
			throw new ServiceException(ErrorCode.ALREADY_ADJUSTMENT_RECEIPT);
		}

		receiptWriter.adjustReceipt(receiptId);
	}

	public void deleteReceipt(Long receiptId, UserPassport userPassport) {
		Receipt receipt = receiptReader.getReceiptWithOwner(receiptId)
			.orElseThrow(() -> {
				log.warn("Receipt 을 찾을 수 없습니다. receiptId: {}", receiptId);
				return new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
			});

		Long storeOwnerId = receipt.getSale().getStore().getOwnerPassport().getUserId();
		if (!storeOwnerId.equals(userPassport.getUserId())) {
			log.warn("Store 의 소유자와 요청자가 다릅니다. ownerId: {}, userId: {}", storeOwnerId, userPassport.getUserId());
			throw new ServiceException(ErrorCode.NOT_VALID_OWNER);
		}
		receiptWriter.deleteReceipt(receiptId);
	}
}
