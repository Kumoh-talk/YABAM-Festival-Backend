package com.pos.menu.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.pos.global.base.entity.BaseEntity;
import com.pos.store.entity.StoreEntity;

import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.store.entity.Store;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "menu_categories",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"store_id", "menu_category_order"})
	})
@NoArgsConstructor
@SQLDelete(sql = "UPDATE menu_categories SET deleted_at = NOW() where id=?")
@SQLRestriction(value = "deleted_at is NULL")
@Getter
public class MenuCategoryEntity extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "menu_category_order", nullable = true)
	private Integer order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private StoreEntity store;

	private MenuCategoryEntity(String name, Integer order, StoreEntity store) {
		this.name = name;
		this.order = order;
		this.store = store;
	}

	private MenuCategoryEntity(Long menuCategoryId) {
		this.id = menuCategoryId;
	}

	public static MenuCategoryEntity of(MenuCategoryInfo menuCategoryInfo, Store store) {
		return new MenuCategoryEntity(
			menuCategoryInfo.getName(),
			menuCategoryInfo.getOrder(),
			StoreEntity.from(store.getId())
		);
	}

	public static MenuCategoryEntity from(Long menuCategoryId) {
		return new MenuCategoryEntity(menuCategoryId);
	}

	public void updateWithoutOrder(MenuCategoryInfo menuCategoryInfo) {
		this.name = menuCategoryInfo.getName();
	}

	public void updateOrder(Integer order) {
		this.order = order;
	}
}
