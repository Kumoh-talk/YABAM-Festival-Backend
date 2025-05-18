package com.pos.store.mapper;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;

import com.pos.store.entity.StoreDetailImageEntity;
import com.pos.store.entity.StoreEntity;
import com.vo.UserPassport;

import domain.pos.store.entity.Store;
import domain.pos.store.entity.StoreInfo;
import domain.pos.store.entity.dto.StoreHeadDto;
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
			storeInfo.getDescription(),
			storeInfo.getHeadImageUrl(),
			storeInfo.getUniversity(),
			storeInfo.getTableTime(),
			storeInfo.getTableCost()
		);
	}

	public static StoreEntity toStoreEntity(Long storeId) {
		return StoreEntity.from(storeId);
	}

	public static StoreInfo toStoreInfo(StoreEntity storeEntity) {
		return StoreInfo.of(
			storeEntity.getName(),
			new Point2D.Double(storeEntity.getLocation().getLatitude(), storeEntity.getLocation().getLongitude()),
			storeEntity.getDescription(),
			storeEntity.getHeadImageUrl(),
			storeEntity.getUniversity(),
			storeEntity.getTableCostPerTime().getTableTime(),
			storeEntity.getTableCostPerTime().getTableCost()
		);
	}

	public static Store toStore(StoreEntity storeEntity) {
		if (storeEntity == null) {
			return null;
		}
		return Store.of(
			storeEntity.getId(),
			storeEntity.isOpen(),
			toStoreInfo(storeEntity),
			UserPassport.of(storeEntity.getOwnerId(), null, null),
			null
		);
	}

	public static Store toStoreWithDetailImages(StoreEntity storeEntity) {
		if (storeEntity == null) {
			return null;
		}
		return Store.of(
			storeEntity.getId(),
			storeEntity.isOpen(),
			toStoreInfo(storeEntity),
			UserPassport.of(storeEntity.getOwnerId(), null, null),
			storeEntity.getStoreDetailImageEntity().stream()
				.map(storeDetailImageEntity -> storeDetailImageEntity.getImageUrl())
				.toList()
		);
	}

	public static Optional<Store> toStore(List<StoreDetailImageEntity> storeDetailImageEntities) {
		if (storeDetailImageEntities.isEmpty()) {
			return Optional.empty();
		}
		StoreEntity storeEntity = storeDetailImageEntities.get(0).getStore();
		List<String> detailImageUrls = storeDetailImageEntities.stream()
			.map(StoreDetailImageEntity::getImageUrl)
			.toList();
		return Optional.of(Store.of(
			storeEntity.getId(),
			storeEntity.isOpen(),
			toStoreInfo(storeEntity),
			UserPassport.of(storeEntity.getOwnerId(), null, null),
			detailImageUrls
		));
	}

	public static Optional<Store> toStoreWithStoreDetailImages(StoreEntity storeEntity) {
		if (storeEntity == null) {
			return Optional.empty();
		}
		return Optional.of(Store.of(
			storeEntity.getId(),
			storeEntity.isOpen(),
			toStoreInfo(storeEntity),
			UserPassport.of(storeEntity.getOwnerId(), null, null),
			storeEntity.getStoreDetailImageEntity().stream()
				.map(StoreDetailImageEntity::getImageUrl)
				.toList()
		));
	}

	public static StoreHeadDto toStoreHeadDto(StoreEntity store) {
		return StoreHeadDto.of(
			store.getId(),
			store.getName(),
			store.isOpen(),
			store.getHeadImageUrl(),
			store.getDescription(),
			store.getStoreDetailImageEntity().stream()
				.map(StoreDetailImageEntity::getImageUrl)
				.toList()
		);
	}
}
