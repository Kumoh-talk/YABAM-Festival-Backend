package com.pos.fixtures.sale;

import com.pos.sale.entity.SaleEntity;
import com.pos.store.entity.StoreEntity;

import domain.pos.sale.entity.Sale;

public class SaleFixture {
	public static SaleEntity GENERAL_SALE(StoreEntity storeEntity) {
		var sale = Sale.createOpenSale(storeEntity.getId());

		return SaleEntity.of(sale);
	}

	public static SaleEntity CLOSED_SALE(StoreEntity storeEntity) {
		var sale = Sale.createOpenSale(storeEntity.getId());

		sale.close(true);

		return SaleEntity.of(sale);
	}
}
