package com.pos.store.entity;

import jakarta.persistence.Column;
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

@Entity
@Table(name = "store_detail_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreDetailImageEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String imageUrl;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private StoreEntity store;

	private StoreDetailImageEntity(String imageUrl, StoreEntity store) {
		this.imageUrl = imageUrl;
		this.store = store;
	}

	public static StoreDetailImageEntity of(String imageUrl, StoreEntity store) {
		return new StoreDetailImageEntity(imageUrl, store);
	}
}
