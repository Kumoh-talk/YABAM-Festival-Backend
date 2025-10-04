package domain.pos.receipt.adapter.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aop.DeadlockRetry;
import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import domain.pos.receipt.entity.v2.domain.Receipt;
import domain.pos.receipt.implement.v2.ReceiptValidator;
import domain.pos.receipt.port.provided.ReceiptCommand;
import domain.pos.receipt.port.required.ReceiptRepository;
import domain.pos.sale.entity.Sale;
import domain.pos.table.entity.Table;
import domain.pos.table.port.required.repository.TableRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReceiptCommandImpl implements ReceiptCommand {
	private final ReceiptValidator receiptValidator;

	private final ReceiptRepository receiptRepository;
	private final TableRepository tableRepository;

	// 수동 영수증 추가 -> 바로 사용 시작
	@DeadlockRetry
	@Transactional
	@Override
	public Receipt create(UserPassport userPassport, Long storeId, UUID tableId) {
		Sale sale = receiptValidator.validateSaleOpen(storeId);
		Table table = receiptValidator.validateTableActive(tableId);

		tableRepository.changeTableActiveStatus(true, table);
		Receipt receipt = Receipt.create(sale.getId(), tableId);

		// TODO : 서브 조회쿼리로 userPassport 검증 + table의 storeId 검증
		return receiptRepository.create(userPassport, storeId, receipt)
			.orElseThrow(() -> new ServiceException(ErrorCode.INVALID_INPUT_VALUE));
	}

	@DeadlockRetry
	@Transactional
	@Override
	public List<Receipt> stopUsage(UserPassport userPassport, List<UUID> receiptIds) {
		List<Receipt> receipts = receiptValidator.validateForStopUsage(receiptIds);

		for (Receipt receipt : receipts) {
			receipt.stopUsage();
		}

		if (receiptRepository.bulkUpdateStopUsageTime(userPassport, receipts) != receipts.size()) {
			throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
		}
		return receipts;
		// TODO : 컨트롤러 응답 시 단위 시간 계산하여 리턴
	}

	@Transactional
	@Override
	public void restartUsage(UserPassport userPassport, List<UUID> receiptIds) {
		// TODO : isAdjustment true 인 영수증은 재시작 불가하도록 막기
		if (receiptRepository.bulkUpdateRestartUsage(userPassport, receiptIds) != receiptIds.size()) {
			throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
		}
	}

	@Transactional
	@Override
	public void adjust(UserPassport userPassport, List<UUID> receiptIds) {
		// TODO : stopUsageTime null, isAdjust true 인 영수증은 정산 불가하도록 막기
		if (receiptRepository.bulkUpdateAdjust(userPassport, receiptIds) != receiptIds.size()) {
			throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
		}

		tableRepository.changeTableActiveStatus(false, receiptIds);
	}

	@Transactional
	@Override
	public void delete(UserPassport userPassport, UUID receiptId) {
		Receipt receipt = receiptRepository.readReceipt(receiptId)
			.orElseThrow(() -> new ServiceException(ErrorCode.RECEIPT_NOT_FOUND));

		receiptRepository.delete(userPassport, receiptId)
			.orElseThrow(() -> new ServiceException(ErrorCode.RECEIPT_NOT_FOUND));
		if (receipt.isAdjustment()) {
			tableRepository.changeTableActiveStatus(false, receiptId);
		}
	}

	@Transactional
	@Override
	public void moveTable(UserPassport userPassport, UUID receiptId, UUID moveTableId) {
		receiptValidator.validateTableActive(moveTableId);
		// TODO : isAdjust true 인 영수증은 테이블 이동 불가하도록 막기
		if (receiptRepository.updateTableId(userPassport, receiptId, moveTableId) == 0) {
			throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
		}

		Receipt receipt = receiptRepository.readReceipt(receiptId)
			.orElseThrow(() -> new ServiceException(ErrorCode.RECEIPT_NOT_FOUND));
		tableRepository.changeTableActiveStatus(true, moveTableId);
		tableRepository.changeTableActiveStatus(false, receipt.getTableId());

	}

	// 테이블 그룹화 : 기준되는 영수증 id, 그룹화할 테이블 id 리스트 -> 영수증 생성 및 사용시간 동기화
	@Transactional
	@Override
	public LocalDateTime syncStartUsageTime(UserPassport userPassport, UUID baseReceiptId, List<UUID> receiptIds) {
		Receipt baseReceipt = receiptRepository.readLock(baseReceiptId)
			.orElseThrow(() -> new ServiceException(ErrorCode.RECEIPT_NOT_FOUND));
		LocalDateTime baseStartUsageTime = baseReceipt.getUsageTime().getStart();

		// TODO : stopUsageTime not null, isAdjust true 인 영수증은 시작시간 동기화 불가하도록 막기
		if (receiptRepository.bulkUpdateStartUsageTime(userPassport, receiptIds, baseStartUsageTime)
			!= receiptIds.size()) {
			throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
		}

		return baseStartUsageTime;
	}
}
