package com.pos.call.repository.dsl;

import org.springframework.data.domain.Slice;

import com.pos.call.entity.CallEntity;

public interface CallDslRepository {
	Slice<CallEntity> getNonCompleteCallsWithReceiptTable(Long storeId, Long lastCallId, int size);
}
