package com.pos.call.repository;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import domain.pos.call.entity.Call;
import domain.pos.call.entity.CallMessage;
import domain.pos.call.repository.CallRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CallRepositoryImpl implements CallRepository {
	@Override
	public void createCall(Long receiptId, CallMessage callMessage) {
	}

	@Override
	public Slice<Call> getNonCompleteCalls(Long storeId) {
		return null;
	}

	@Override
	public void modifyCallComplete(Long callId) {

	}
}
