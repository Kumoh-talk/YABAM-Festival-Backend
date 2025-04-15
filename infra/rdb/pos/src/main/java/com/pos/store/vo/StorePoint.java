package com.pos.store.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class StorePoint {
	@Column(name = "latitude", nullable = false)
	private Double latitude;
	@Column(name = "longitude", nullable = false)
	private Double longitude;

	private StorePoint(Double latitude, Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public static StorePoint of(Double latitude, Double longitude) {
		return new StorePoint(latitude, longitude);
	}
}
