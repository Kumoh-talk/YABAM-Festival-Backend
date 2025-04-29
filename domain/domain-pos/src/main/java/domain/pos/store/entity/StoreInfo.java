package domain.pos.store.entity;

import java.awt.geom.Point2D;

import lombok.Getter;

@Getter
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
		this.storeName = storeName;
		this.location = location;
		this.description = description;
		this.headImageUrl = headImageUrl;
		this.university = university;
		this.tableTime = tableTime;
		this.tableCost = tableCost;
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
