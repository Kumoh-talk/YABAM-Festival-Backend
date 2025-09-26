package com.pos.call.repository.dsl;

import org.springframework.data.domain.Slice;

import com.pos.call.entity.CallEntity;

import domain.pos.call.entity.dto.CallInfoDto;

public interface CallDslRepository {
	Slice<CallEntity> getNonCompleteCallsWithReceiptTable(Long storeId, Long lastCallId, int size);

	boolean isExistsCallOwner(Long callId, Long userId);

	Slice<CallInfoDto> getNonCompleteCallsWithReceiptTableV2(Long saleId, Long lastCallId, int pageSize);
}
