package com.pos.fixtures.sale;

import com.pos.sale.entity.SaleEntity;
import com.pos.store.entity.StoreEntity;

public class SaleFixture {
	public static SaleEntity GENERAL_SALE(StoreEntity storeEntity) {
		return SaleEntity.from(storeEntity);
	}
}
