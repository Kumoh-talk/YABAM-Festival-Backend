package domain.pos.store.entity;

import static java.util.Objects.*;

import java.awt.geom.Point2D;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class StoreInfo {
	private final String storeName;
	private final Point2D.Double location;
	private final String description;
	private final String headImageUrl;
	private final String university;
	private final Integer tableTime;
	private final Integer tableCost;

	private StoreInfo(String storeName, Point2D.Double location, String description, String headImageUrl,
		String university, Integer tableTime, Integer tableCost) {
		this.storeName = requireNonNull(storeName);
		this.location = requireNonNull(location);
		this.description = description;
		this.headImageUrl = requireNonNull(headImageUrl);
		this.university = requireNonNull(university);
		this.tableTime = tableTime;
		this.tableCost = requireNonNull(tableCost);
	}

	public static StoreInfo of(String storeName, Point2D.Double location, String desciption, String headImageUrl,
		String university, Integer tableTime, Integer tableCost) {
		return new StoreInfo(
			storeName,
			location,
			desciption,
			headImageUrl,
			university,
			tableTime,
			tableCost);
	}
}
