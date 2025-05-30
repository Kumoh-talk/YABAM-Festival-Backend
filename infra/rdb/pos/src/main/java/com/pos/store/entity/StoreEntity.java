package com.pos.store.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.pos.global.base.entity.BaseEntity;
import com.pos.store.vo.StorePoint;
import com.pos.store.vo.TableCostPerTime;

import domain.pos.store.entity.StoreInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stores")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SQLDelete(sql = "UPDATE stores SET deleted_at = NOW() where id=?")
@SQLRestriction(value = "deleted_at is NULL")
public class StoreEntity extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "owner_id", nullable = false)
	private Long ownerId;

	@Column(name = "is_open", nullable = false)
	private boolean isOpen;

	@Column(name = "name", nullable = false)
	private String name;

	@Embedded
	private StorePoint location;

	@Column(name = "description")
	private String description;

	@Column(name = "head_image_url")
	private String headImageUrl;

	@Column(name = "university", nullable = false)
	private String university;

	@Embedded
	private TableCostPerTime tableCostPerTime;

	@OneToMany(mappedBy = "store", orphanRemoval = false, fetch = FetchType.LAZY)
	private List<StoreDetailImageEntity> storeDetailImageEntity = new ArrayList<>();

	private StoreEntity(Long ownerId, boolean isOpen, String name, StorePoint location, String description,
		String headImageUrl, String university, TableCostPerTime tableCostPerTime) {
		this.ownerId = ownerId;
		this.isOpen = isOpen;
		this.name = name;
		this.location = location;
		this.description = description;
		this.headImageUrl = headImageUrl;
		this.university = university;
		this.tableCostPerTime = tableCostPerTime;
	}

	private StoreEntity(Long storeId) {
		this.id = storeId;
	}

	public static StoreEntity of(
		Long ownerId,
		boolean isOpen,
		String name,
		Double latitude,
		Double longitude,
		String description,
		String headImageUrl,
		String university,
		Integer tableTime,
		Integer tableCost) {
		return new StoreEntity(
			ownerId,
			isOpen,
			name,
			StorePoint.of(latitude, longitude),
			description,
			headImageUrl,
			university,
			TableCostPerTime.of(tableTime, tableCost)
		);
	}

	public static StoreEntity from(Long storeId) {
		return new StoreEntity(storeId);
	}

	public void changeStoreInfo(StoreInfo requestChangeStoreInfo) {
		this.name = requestChangeStoreInfo.getStoreName();
		this.headImageUrl = requestChangeStoreInfo.getHeadImageUrl();
		this.description = requestChangeStoreInfo.getDescription();
		this.university = requestChangeStoreInfo.getUniversity();
		this.location = StorePoint.of(
			requestChangeStoreInfo.getLocation().x,
			requestChangeStoreInfo.getLocation().y
		);
		this.tableCostPerTime = TableCostPerTime.of(
			requestChangeStoreInfo.getTableTime(),
			requestChangeStoreInfo.getTableCost()
		);
	}
}
