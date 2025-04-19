package com.pos.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pos.sale.repository.SaleJpaRepository;
import com.pos.store.repository.StoreJpaRepository;
import com.pos.table.repository.TableJpaRepository;

@Component
public class BuilderSupporter {
	@Autowired
	private StoreJpaRepository storeJpaRepository;

	@Autowired
	private TableJpaRepository tableJpaRepository;

	@Autowired
	private SaleJpaRepository saleJpaRepository;

	public StoreJpaRepository getStoreJpaRepository() {
		return storeJpaRepository;
	}

	public TableJpaRepository getTableJpaRepository() {
		return tableJpaRepository;
	}

	public SaleJpaRepository getSaleJpaRepository() {
		return saleJpaRepository;
	}
}
