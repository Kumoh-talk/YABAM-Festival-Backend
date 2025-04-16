package com.pos.table.entity;

import com.pos.global.base.entity.BaseEntity;
import com.pos.store.entity.StoreEntity;
import com.pos.table.vo.TableNumber;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "tables")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TableEntity extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Embedded
	private TableNumber tableNumber;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private StoreEntity store;

	private TableEntity(TableNumber tableNumber, Boolean isActive, StoreEntity store) {
		this.tableNumber = tableNumber;
		this.isActive = isActive;
		this.store = store;
	}

	public static TableEntity of(TableNumber tableNumber, Boolean isActive, StoreEntity store) {
		return new TableEntity(
			tableNumber,
			isActive,
			store
		);
	}
}
