package com.pos.fixtures.store;

import com.pos.store.entity.StoreEntity;
import com.pos.store.mapper.StoreMapper;

import domain.pos.store.entity.Store;

public class StoreEntityFixture {

	public static StoreEntity CUSTOME_STORE_ENTITY(Store store) {
		return StoreMapper.toStoreEntity(
			store.getOwnerPassport(),
			store.getStoreInfo(),
			store.getIsOpen()
		);
	}

}
