package com.pos.builder;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pos.sale.entity.SaleEntity;
import com.pos.store.entity.StoreEntity;
import com.pos.table.entity.TableEntity;

@Component
public class TestFixtureBuilder {
	@Autowired
	private BuilderSupporter bs;

	public StoreEntity buildStoreEntity(StoreEntity storeEntity) {
		return bs.getStoreJpaRepository().save(storeEntity);
	}

	public List<TableEntity> buildTableEntityList(List<TableEntity> tableEntityList) {
		return bs.getTableJpaRepository().saveAll(tableEntityList);
	}

	public SaleEntity buildSaleEntity(SaleEntity saleEntity) {
		return bs.getSaleJpaRepository().save(saleEntity);
	}
}
