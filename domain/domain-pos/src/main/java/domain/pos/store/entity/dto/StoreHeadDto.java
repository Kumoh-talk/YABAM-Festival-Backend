package domain.pos.store.entity.dto;

import java.util.List;

import lombok.Getter;

@Getter
public class StoreHeadDto {
	private final Long storeId;
	private final String storeName;
	private final Boolean isOpened;
	private final String thumbnailUrl;
	private final String location;
	private final String universityName;
	private final String description;
	private final List<String> detailImageUrls;

	private StoreHeadDto(Long storeId, String storeName, Boolean isOpened, String thumbnailUrl, String location,
		String universityName, String description, List<String> detailImageUrls) {
		this.storeId = storeId;
		this.storeName = storeName;
		this.isOpened = isOpened;
		this.thumbnailUrl = thumbnailUrl;
		this.location = location;
		this.universityName = universityName;
		this.description = description;
		this.detailImageUrls = detailImageUrls != null ? List.copyOf(detailImageUrls) : List.of();
	}

	public static StoreHeadDto of(Long storeId, String storeName, Boolean isOpened, String thumbnailUrl,
		String location, String universityName, String description, List<String> detailImageUrls) {
		return new StoreHeadDto(storeId, storeName, isOpened, thumbnailUrl, location, universityName, description,
			detailImageUrls);
	}
}
