package com.pos.store.mapper;

import java.awt.geom.Point2D;

import com.pos.store.entity.StoreEntity;

import domain.pos.member.entity.UserPassport;
import domain.pos.store.entity.Store;
import domain.pos.store.entity.StoreInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreMapper {
	public static StoreEntity toStoreEntity(UserPassport userPassport, StoreInfo storeInfo, boolean isOpen) {
		return StoreEntity.of(
			userPassport.getUserId(),
			isOpen,
			storeInfo.getStoreName(),
			storeInfo.getLocation().getX(),
			storeInfo.getLocation().getY(),
			storeInfo.getDesciption(),
			storeInfo.getHeadImageUrl(),
			storeInfo.getUniversity()
		);
	}

	public static StoreInfo toStoreInfo(StoreEntity storeEntity) {
		return StoreInfo.of(
			storeEntity.getName(),
			new Point2D.Double(storeEntity.getLocation().getLatitude(), storeEntity.getLocation().getLongitude()),
			storeEntity.getDescription(),
			storeEntity.getHeadImageUrl(),
			storeEntity.getUniversity()
		);
	}

	public static Store toStore(StoreEntity storeEntity) {
		return Store.of(
			storeEntity.getId(),
			storeEntity.isOpen(),
			toStoreInfo(storeEntity),
			UserPassport.of(storeEntity.getId(), null, null)
		);
	}
}
