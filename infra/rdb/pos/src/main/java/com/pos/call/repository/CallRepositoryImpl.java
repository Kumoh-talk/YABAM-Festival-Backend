package com.pos.call.repository;

import java.util.UUID;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.pos.call.entity.CallEntity;
import com.pos.call.mapper.CallMapper;
import com.pos.call.repository.jpa.CallJpaRepository;
import com.vo.UserPassport;

import domain.pos.call.entity.Call;
import domain.pos.call.entity.CallMessage;
import domain.pos.call.repository.CallRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CallRepositoryImpl implements CallRepository {
	private final CallJpaRepository callJpaRepository;

	@Override
	public void createCall(UUID receiptId, Long saleId, CallMessage callMessage) {
		CallEntity callEntity = CallMapper.toCallEntity(receiptId, saleId, callMessage);
		callJpaRepository.save(callEntity);
	}

	@Override
	public Slice<Call> getNonCompleteCalls(Long saleId, Long lastCallId, int size) {
		return CallMapper.toCallSlice(callJpaRepository.getNonCompleteCallsWithReceiptTable(saleId, lastCallId, size));
	}

	@Override
	@Transactional
	public void modifyCallComplete(Long callId) {
		CallEntity callEntity = callJpaRepository.findById(callId)
			.orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND_CALL));
		callEntity.completeCall();
	}

	@Override
	public boolean isExistsCallOwner(Long callId, UserPassport ownerPassport) {
		return callJpaRepository.isExistsCallOwner(callId, ownerPassport.getUserId());
	}
}
