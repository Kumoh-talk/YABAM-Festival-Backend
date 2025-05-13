package domain.pos.call.repository;

import org.springframework.data.domain.Slice;

import domain.pos.call.entity.Call;
import domain.pos.call.entity.CallMessage;

public interface CallRepository {
	void createCall(Long receiptId, CallMessage callMessage);

	Slice<Call> getNonCompleteCalls(Long storeId);

	void modifyCallComplete(Long callId);
}
