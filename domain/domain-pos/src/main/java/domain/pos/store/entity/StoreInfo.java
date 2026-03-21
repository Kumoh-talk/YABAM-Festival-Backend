package domain.pos.store.entity;

import static java.util.Objects.*;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class StoreInfo {
	private final String storeName;
	private final String location;
	private final String description;
	private final String thumbnailUrl;
	private final String universityName;
	private final Integer tableTime;
	private final Integer tableCost;

	private StoreInfo(String storeName, String location, String description, String thumbnailUrl,
		String universityName, Integer tableTime, Integer tableCost) {
		this.storeName = requireNonNull(storeName);
		this.location = requireNonNull(location);
		this.description = description;
		this.thumbnailUrl = requireNonNull(thumbnailUrl);
		this.universityName = requireNonNull(universityName);
		this.tableTime = tableTime;
		this.tableCost = requireNonNull(tableCost);
	}

	public static StoreInfo of(String storeName, String location, String description, String thumbnailUrl,
		String universityName, Integer tableTime, Integer tableCost) {
		return new StoreInfo(storeName, location, description, thumbnailUrl, universityName, tableTime, tableCost);
	}
}
