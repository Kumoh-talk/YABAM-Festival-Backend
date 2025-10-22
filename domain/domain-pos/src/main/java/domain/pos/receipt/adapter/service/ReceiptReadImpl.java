package domain.pos.receipt.adapter.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import domain.pos.receipt.entity.v2.domain.Receipt;
import domain.pos.receipt.implement.v2.ReceiptValidator;
import domain.pos.receipt.port.provided.ReceiptRead;
import domain.pos.receipt.port.required.ReceiptRepository;
import domain.pos.table.port.required.repository.TableRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReceiptReadImpl implements ReceiptRead {
	private final ReceiptValidator receiptValidator;

	private final ReceiptRepository receiptRepository;
	private final TableRepository tableRepository;

	@Override
	public List<Receipt> readAllTableNonAdjusts(UserPassport userPassport, Long saleId) {
		receiptValidator.validateSaleStoreOwner(userPassport, saleId);

		List<Receipt> receipts = receiptRepository.readNonAdjusts(saleId);
		receipts.sort(Comparator.comparing(Receipt::getTableId));

		return receipts;
		// TODO : 컨트롤러 응답 시 단위 시간 계산하여 리턴
	}

	@Override
	public Page<Receipt> readAdjustsPageBySale(UserPassport userPassport, Pageable pageable, Long saleId) {
		receiptValidator.validateSaleStoreOwner(userPassport, saleId);

		Page<Receipt> receiptPage = receiptRepository.readAdjustedPage(pageable, saleId);
		return receiptPage;
	}

	@Override
	public UUID readNonAdjustReceiptId(UUID tableId) {
		return receiptRepository.readNonAdjusts(tableId)
			.map(Receipt::getId)
			.orElseGet(() -> {
				tableRepository.findById(tableId)
					.orElseThrow(() -> new ServiceException(ErrorCode.TABLE_NOT_FOUND));

				throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
			});
	}
}
