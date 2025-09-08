package domain.pos.call.implement;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import domain.pos.call.entity.Call;
import domain.pos.call.port.required.repository.CallRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CallReader {
	private final CallRepository callRepository;

	public Slice<Call> getNonCompleteCalls(Long saleId, Long lastCallId, int size) {
		return callRepository.getNonCompleteCalls(saleId, lastCallId, size);
	}

	public void validateCallOwner(Long callId, UserPassport ownerPassport) {
		if (!callRepository.isExistsCallOwner(callId, ownerPassport)) {
			throw new ServiceException(ErrorCode.NOT_VALID_CALL_OWNER);
		}
	}
}
