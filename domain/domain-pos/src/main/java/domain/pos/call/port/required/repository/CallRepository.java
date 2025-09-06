package domain.pos.call.port.required.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Slice;

import com.vo.UserPassport;

import domain.pos.call.entity.Call;
import domain.pos.call.entity.CallMessage;
import domain.pos.call.entity.dto.CallInfoDto;

public interface CallRepository {
	void createCall(UUID receiptId, Long storeId, CallMessage callMessage);

	Slice<Call> getNonCompleteCalls(Long saleId, Long lastCallId, int size);

	void modifyCallComplete(Long callId);

	boolean isExistsCallOwner(Long callId, UserPassport ownerPassport);

	Call save(Call call);

	Optional<Call> findById(Long callId);

	Slice<CallInfoDto> getNonCompleteCallsBySaleId(Long saleId, Long lastCallId, int pageSize);
}
