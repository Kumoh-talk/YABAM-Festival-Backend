package com.pos.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pos.store.repository.StoreJpaRepository;
import com.pos.table.repository.TableJpaRepository;

@Component
public class BuilderSupporter {
	@Autowired
	private StoreJpaRepository storeJpaRepository;

	@Autowired
	private TableJpaRepository tableJpaRepository;

	public StoreJpaRepository getStoreJpaRepository() {
		return storeJpaRepository;
	}

	public TableJpaRepository getTableJpaRepository() {
		return tableJpaRepository;
	}
}
