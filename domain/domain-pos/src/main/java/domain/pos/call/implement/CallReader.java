package domain.pos.call.implement;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import domain.pos.call.entity.Call;
import domain.pos.call.repository.CallRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CallReader {
	private final CallRepository callRepository;

	public Slice<Call> getNonCompleteCalls(Long storeId) {
		return callRepository.getNonCompleteCalls(storeId);
	}
}
