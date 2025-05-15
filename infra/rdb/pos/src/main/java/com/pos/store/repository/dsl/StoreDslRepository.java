package com.pos.store.repository.dsl;

import org.springframework.data.domain.Slice;

import domain.pos.store.entity.dto.StoreHeadDto;

public interface StoreDslRepository {
	Slice<StoreHeadDto> findStoreHeadsByStoreIdCursor(
		Long lastStoreId,
		int size);
}
