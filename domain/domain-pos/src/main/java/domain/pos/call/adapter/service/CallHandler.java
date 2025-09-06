package domain.pos.call.adapter.service;

import static com.exception.ErrorCode.*;
import static com.exception.State.*;

import java.util.UUID;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.exception.ServiceException;
import com.vo.UserPassport;

import domain.pos.call.entity.Call;
import domain.pos.call.entity.dto.CallInfoDto;
import domain.pos.call.port.provided.CallCommand;
import domain.pos.call.port.provided.CallRead;
import domain.pos.call.port.required.repository.CallRepository;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class CallHandler implements CallCommand, CallRead {
	private final ReceiptRepository receiptRepository;
	private final CallRepository callRepository;

	@Override
	public Call createCall(final UUID receiptId, final String message) {
		var receipt = receiptRepository.getReceiptById(receiptId)
			.orElseThrow(() -> new ServiceException(RECEIPT_NOT_FOUND));

		ifState(isAdjustment(receipt), ALREADY_ADJUSTMENT_RECEIPT);

		var call = Call.create(receipt.getSale().getId(), receiptId, message);

		return callRepository.save(call);
	}

	private static boolean isAdjustment(Receipt receipt) {
		return receipt.getReceiptInfo().isAdjustment();
	}

	@Override
	public Call completeCall(UserPassport passport, final Long callId) {
		ifState(isNotCallOwner(passport, callId), NOT_VALID_CALL_OWNER);

		var call = callRepository.findById(callId)
			.orElseThrow(() -> new ServiceException(NOT_FOUND_CALL));

		call.complete();

		return callRepository.save(call);
	}

	private boolean isNotCallOwner(UserPassport passport, Long callId) {
		return !callRepository.isExistsCallOwner(callId, passport);
	}

	@Override
	public Slice<CallInfoDto> getNonCompleteCalls(final UserPassport ownerPassport, final Long saleId,
		final Long lastCallId,
		int pageSize) {
		return callRepository.getNonCompleteCallsBySaleId(saleId, lastCallId, pageSize);
	}
}
