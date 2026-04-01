package com.pos.sale.mapper;

import java.time.LocalDateTime;

import com.pos.sale.entity.SaleEntity;
import com.pos.store.entity.StoreEntity;
import com.pos.store.mapper.StoreMapper;

import domain.pos.sale.entity.Sale;
import domain.pos.store.entity.Store;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class SaleMapper {
	public static SaleEntity toSaleEntity(Store store) {
		return SaleEntity.from(StoreEntity.from(store.getId()));
	}

	public static Sale toSale(SaleEntity saveSaleEntity, Store previousStore) {
		return Sale.of(
			saveSaleEntity.getId(),
			saveSaleEntity.getOpenDateTime(),
			saveSaleEntity.getCloseDateTime(),
			previousStore);
	}

	public static Sale toSale(SaleEntity saleEntity) {
		if (saleEntity == null) {
			return null;
		}
		return Sale.of(
			saleEntity.getId(),
			saleEntity.getOpenDateTime(),
			saleEntity.getCloseDateTime(),
			saleEntity.getStore().getId()
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
			savedSale.getId(),
			savedSale.getOpenDateTime(),
			now,
			closeStore
		);
	}
}
