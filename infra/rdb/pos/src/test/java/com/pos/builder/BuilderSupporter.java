package com.pos.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pos.store.repository.StoreJpaRepository;

@Component
public class BuilderSupporter {
	@Autowired
	private StoreJpaRepository storeJpaRepository;

	public StoreJpaRepository getStoreJpaRepository() {
		return storeJpaRepository;
	}
}
