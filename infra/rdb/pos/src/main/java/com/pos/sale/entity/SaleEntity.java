package com.pos.sale.entity;

import java.time.LocalDateTime;

import com.pos.store.entity.StoreEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "sales")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SaleEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private LocalDateTime openDateTime;

	@Column(nullable = true)
	private LocalDateTime closeDateTime;

	@ManyToOne
	@JoinColumn(name = "store_id")
	private StoreEntity store;
}
