package domain.pos.call.service;

import java.util.UUID;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import domain.pos.call.entity.Call;
import domain.pos.call.entity.CallMessage;
import domain.pos.call.implement.CallReader;
import domain.pos.call.implement.CallWriter;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.implement.ReceiptReader;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CallService {
	private final ReceiptReader receiptReader;
	private final CallWriter callWriter;
	private final CallReader callReader;

	public void postCall(final UUID receiptId, final CallMessage callMessage) {
		Receipt receipt = receiptReader.getReceiptWithTableAndStore(receiptId)
			.orElseThrow(() -> new ServiceException(ErrorCode.RECEIPT_NOT_FOUND));
		if (!receipt.getSale().getCloseDateTime().isEmpty() || !receipt.getSale().getStore().getIsOpen()) {
			throw new ServiceException(ErrorCode.CONFLICT_CLOSE_STORE);
		}
		if (!receipt.getTable().getIsActive()) {
			throw new ServiceException(ErrorCode.TABLE_NOT_ACTIVE);
		}

		callWriter.createCall(receiptId, receipt.getSale().getId(), callMessage);
	}

	// TODO : 해당 사장 권한 validation 코드를 추가해야할듯한데 어디 범위까지 해야할지 고민(ex. store table...)
	public Slice<Call> getNonCompleteCalls(final UserPassport ownerPassport, final Long saleId, final Long lastCallId,
		int pageSize) {
		return callReader.getNonCompleteCalls(saleId, lastCallId, pageSize);
	}

	public void completeCall(final UserPassport ownerPassport, final Long callId) {
		callReader.validateCallOwner(callId, ownerPassport);
		callWriter.completeCall(callId);
	}
}
