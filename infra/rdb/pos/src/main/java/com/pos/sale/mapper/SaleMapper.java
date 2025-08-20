package com.pos.sale.mapper;

import java.time.LocalDateTime;

import com.pos.sale.entity.SaleEntity;
import com.pos.store.entity.StoreEntity;
import com.pos.store.mapper.StoreMapper;

import domain.pos.store.entity.Sale;
import domain.pos.store.entity.Store;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class SaleMapper {
	// SaleEntity -> Sale
	public static SaleEntity toSaleEntity(Store store) {
		StoreEntity storeEntity = StoreEntity.from(store.getId());
		return SaleEntity.from(storeEntity);
	}

	public static Sale toSale(SaleEntity saveSaleEntity, Store previousStore) {
		return Sale.of(
			saveSaleEntity.getId(),
			saveSaleEntity.getOpenDateTime(),
			saveSaleEntity.getCloseDateTime(),
			previousStore);
	}

	public static Sale toSale(SaleEntity saleEntity) {
		return Sale.of(
			saleEntity.getId(),
			saleEntity.getOpenDateTime(),
			saleEntity.getCloseDateTime(),
			null
		);
	}

	public static Sale toSaleWithStore(SaleEntity saleEntity) {
		if (saleEntity == null) {
			return null;
		}
		return Sale.of(
			saleEntity.getId(),
			saleEntity.getOpenDateTime(),
			saleEntity.getCloseDateTime(),
			StoreMapper.toStore(saleEntity.getStore())
		);
	}

	public static Sale toClosedSale(Sale savedSale, Store closeStore, LocalDateTime now) {
		return Sale.of(
			savedSale.getSaleId(),
			savedSale.getOpenDateTime(),
			now,
			closeStore
		);
	}
}
