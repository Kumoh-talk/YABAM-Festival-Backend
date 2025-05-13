package domain.pos.call.implement;

import org.springframework.stereotype.Component;

import domain.pos.call.entity.CallMessage;
import domain.pos.call.repository.CallRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CallWriter {
	private final CallRepository callRepository;

	public void createCall(Long receiptId, Long saleId, CallMessage callMessage) {
		callRepository.createCall(receiptId, saleId, callMessage);
	}

	public void completeCall(Long callId) {
		callRepository.modifyCallComplete(callId);
	}
}
