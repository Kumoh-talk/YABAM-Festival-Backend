package com.pos.menu.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.pos.global.base.entity.BaseEntity;
import com.pos.store.entity.StoreEntity;

import domain.pos.menu.entity.MenuInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "menus",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"menu_category_id", "menu_order"})
	})
@NoArgsConstructor
@SQLDelete(sql = "UPDATE menus SET deleted_at = NOW() where id=?")
@SQLRestriction(value = "deleted_at is NULL")
@Getter
public class MenuEntity extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "menu_order", nullable = true)
	private Integer order;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "price", nullable = false)
	private Integer price;

	@Column(name = "description")
	private String description;

	@Column(name = "image_url")
	private String imageUrl;

	@Column(name = "is_sold_out", nullable = false)
	private boolean isSoldOut;

	@Column(name = "is_recommended", nullable = false)
	private boolean isRecommended;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private StoreEntity store;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menu_category_id", nullable = false)
	private MenuCategoryEntity menuCategory;

	@Builder
	private MenuEntity(Integer order, String name, Integer price, String description, String imageUrl,
		boolean isSoldOut,
		boolean isRecommended, StoreEntity store, MenuCategoryEntity menuCategory) {
		this.order = order;
		this.name = name;
		this.price = price;
		this.description = description;
		this.imageUrl = imageUrl;
		this.isSoldOut = isSoldOut;
		this.isRecommended = isRecommended;
		this.store = store;
		this.menuCategory = menuCategory;
	}

	private MenuEntity(Long menuId) {
		this.id = menuId;
	}

	public static MenuEntity of(MenuInfo menuInfo, StoreEntity storeEntity, MenuCategoryEntity menuCategoryEntity) {
		return MenuEntity.builder()
			.order(menuInfo.getOrder())
			.name(menuInfo.getName())
			.price(menuInfo.getPrice())
			.description(menuInfo.getDescription())
			.imageUrl(menuInfo.getImageUrl())
			.isSoldOut(menuInfo.isSoldOut())
			.isRecommended(menuInfo.isRecommended())
			.store(storeEntity)
			.menuCategory(menuCategoryEntity)
			.build();
	}

	public static MenuEntity from(Long menuId) {
		return new MenuEntity(
			menuId
		);
	}

	public void updateWithoutOrder(MenuInfo menuInfo) {
		this.name = menuInfo.getName();
		this.price = menuInfo.getPrice();
		this.description = menuInfo.getDescription();
		this.imageUrl = menuInfo.getImageUrl();
		this.isSoldOut = menuInfo.isSoldOut();
		this.isRecommended = menuInfo.isRecommended();
	}

	public void updateOrder(Integer order) {
		this.order = order;
	}
}
