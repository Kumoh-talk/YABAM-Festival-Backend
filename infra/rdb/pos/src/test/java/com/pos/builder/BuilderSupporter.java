package com.pos.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pos.menu.repository.jpa.MenuCategoryJpaRepository;
import com.pos.menu.repository.jpa.MenuJpaRepository;
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

	@Autowired
	private MenuJpaRepository menuJpaRepository;

	@Autowired
	private MenuCategoryJpaRepository menuCategoryJpaRepository;

	public StoreJpaRepository getStoreJpaRepository() {
		return storeJpaRepository;
	}

	public TableJpaRepository getTableJpaRepository() {
		return tableJpaRepository;
	}

	public SaleJpaRepository getSaleJpaRepository() {
		return saleJpaRepository;
	}

	public MenuJpaRepository getMenuJpaRepository() {
		return menuJpaRepository;
	}

	public MenuCategoryJpaRepository getMenuCategoryJpaRepository() {
		return menuCategoryJpaRepository;
	}
}
