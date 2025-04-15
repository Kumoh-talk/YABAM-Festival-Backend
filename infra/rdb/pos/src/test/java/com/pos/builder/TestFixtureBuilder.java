package com.pos.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pos.store.entity.StoreEntity;

@Component
public class TestFixtureBuilder {
	@Autowired
	private BuilderSupporter bs;

	public StoreEntity buildStoreEntity(StoreEntity storeEntity) {
		return bs.getStoreJpaRepository().save(storeEntity);
	}
}
