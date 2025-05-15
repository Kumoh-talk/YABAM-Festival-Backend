package domain.pos.call.repository;

import java.util.UUID;

import org.springframework.data.domain.Slice;

import com.vo.UserPassport;

import domain.pos.call.entity.Call;
import domain.pos.call.entity.CallMessage;

public interface CallRepository {
	void createCall(UUID receiptId, Long storeId, CallMessage callMessage);

	Slice<Call> getNonCompleteCalls(Long saleId, Long lastCallId, int size);

	void modifyCallComplete(Long callId);

	boolean isExistsCallOwner(Long callId, UserPassport ownerPassport);
}
