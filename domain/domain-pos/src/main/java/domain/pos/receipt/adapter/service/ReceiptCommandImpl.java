package domain.pos.receipt.adapter.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import domain.pos.receipt.entity.v2.domain.Receipt;
import domain.pos.receipt.entity.v2.dto.CreateValidationResult;
import domain.pos.receipt.implement.v2.ReceiptValidator;
import domain.pos.receipt.port.provided.ReceiptCommand;
import domain.pos.receipt.port.required.ReceiptRepository;
import domain.pos.table.port.required.repository.TableRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReceiptCommandImpl implements ReceiptCommand {
	private final ReceiptValidator receiptValidator;

	private final ReceiptRepository receiptRepository;
	private final TableRepository tableRepository;

	// 수동 영수증 추가 -> 바로 사용 시작
	@Transactional
	@Override
	public Receipt create(UserPassport userPassport, Long storeId, UUID tableId) {
		CreateValidationResult validationResult = receiptValidator.validateForCreate(userPassport, storeId, tableId);

		tableRepository.changeTableActiveStatus(true, validationResult.table());
		Receipt receipt = Receipt.create(validationResult.sale().getId(), tableId);

		return receiptRepository.create(userPassport, storeId, receipt)
			.orElseThrow(() -> new ServiceException(ErrorCode.INVALID_INPUT_VALUE));
	}

	@Transactional
	@Override
	public List<Receipt> stopUsage(UserPassport userPassport, List<UUID> receiptIds) {
		var receipts = receiptValidator.validateForStopUsage(userPassport, receiptIds);

		receipts.forEach(Receipt::stopUsage);

		if (receiptRepository.bulkUpdateStopUsageTime(receipts) != receipts.size()) {
			throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
		}
		return receipts;
		// TODO : 컨트롤러 응답 시 단위 시간 계산하여 리턴
	}

	@Transactional
	@Override
	public void restartUsage(UserPassport userPassport, List<UUID> receiptIds) {
		receiptValidator.validateAllStoreOwners(userPassport, receiptIds);

		List<Receipt> receipts = readReceiptsWithWriteLock(receiptIds);
		receipts.forEach(Receipt::restartUsage);

		if (receiptRepository.bulkUpdateRestartUsage(receiptIds) != receiptIds.size()) {
			throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
		}
	}

	@Transactional
	@Override
	public void adjust(UserPassport userPassport, List<UUID> receiptIds) {
		receiptValidator.validateAllStoreOwners(userPassport, receiptIds);

		List<Receipt> receipts = readReceiptsWithWriteLock(receiptIds);
		receipts.forEach(Receipt::adjust);

		if (receiptRepository.bulkUpdateAdjust(receiptIds) != receiptIds.size()) {
			throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
		}
		tableRepository.changeTableActiveStatus(false, receiptIds);
	}

	@Transactional
	@Override
	public void delete(UserPassport userPassport, UUID receiptId) {
		receiptValidator.validateSingleStoreOwner(userPassport, receiptId);

		Receipt receipt = readReceipt(receiptId);
		receiptRepository.delete(receiptId)
			.orElseThrow(() -> new ServiceException(ErrorCode.RECEIPT_NOT_FOUND));
		if (!receipt.isAdjustment()) {
			tableRepository.changeTableActiveStatus(false, receiptId);
		}
	}

	@Transactional
	@Override
	public void moveTable(UserPassport userPassport, UUID receiptId, UUID moveTableId) {
		receiptValidator.validateForMoveTable(userPassport, receiptId, moveTableId);

		Receipt receipt = readReceiptWithWriteLock(receiptId);
		UUID currentTableId = receipt.getTableId();
		receipt.moveTable(moveTableId);

		if (receiptRepository.updateTableId(receipt) == 0) {
			throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
		}
		tableRepository.changeTableActiveStatus(true, moveTableId);
		tableRepository.changeTableActiveStatus(false, currentTableId);
	}

	// 테이블 그룹화 : 기준되는 영수증 id, 그룹화할 테이블 id 리스트 -> 영수증 생성 및 사용시간 동기화
	@Transactional
	@Override
	public LocalDateTime syncStartUsageTime(UserPassport userPassport, UUID baseReceiptId, List<UUID> receiptIds) {
		receiptValidator.validateForSyncStart(userPassport, baseReceiptId, receiptIds);

		LocalDateTime baseStartUsageTime = readReceiptWithReadLock(baseReceiptId).getUsageTime().getStart();
		readReceiptsWithWriteLock(receiptIds)
			.forEach(syncReceipt -> syncReceipt.syncStartUsageTime(baseStartUsageTime));
		if (receiptRepository.bulkUpdateStartUsageTime(receiptIds, baseStartUsageTime)
			!= receiptIds.size()) {
			throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
		}

		return baseStartUsageTime;
	}

	private Receipt readReceipt(UUID receiptId) {
		return receiptRepository.readReceipt(receiptId)
			.orElseThrow(() -> new ServiceException(ErrorCode.RECEIPT_NOT_FOUND));
	}

	private List<Receipt> readReceiptsWithWriteLock(List<UUID> receiptIds) {
		List<Receipt> receipts = receiptRepository.writeLock(receiptIds);
		if (receipts.size() != receiptIds.size()) {
			throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
		}
		return receipts;
	}

	private Receipt readReceiptWithWriteLock(UUID receiptId) {
		return receiptRepository.writeLock(receiptId)
			.orElseThrow(() -> new ServiceException(ErrorCode.RECEIPT_NOT_FOUND));
	}

	private Receipt readReceiptWithReadLock(UUID receiptId) {
		return receiptRepository.readLock(receiptId)
			.orElseThrow(() -> new ServiceException(ErrorCode.RECEIPT_NOT_FOUND));
	}
}
