package com.pos.store.repository.dsl;

import java.util.Optional;

import org.springframework.data.domain.Slice;

import com.pos.store.entity.StoreEntity;

import domain.pos.store.entity.dto.StoreHeadDto;

public interface StoreDslRepository {
	Optional<StoreEntity> findStoreWithDetailImageByStoreId(Long storeId);

	Slice<StoreHeadDto> findStoreHeadsByStoreIdCursor(
		Long lastStoreId,
		int size);
}
