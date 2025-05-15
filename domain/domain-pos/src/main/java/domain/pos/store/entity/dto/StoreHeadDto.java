package domain.pos.store.entity.dto;

import java.util.List;

import lombok.Getter;

@Getter
public class StoreHeadDto {
	private final Long storeId;
	private final String storeName;
	private final Boolean isOpened;
	private final String headImageUrl;
	private final String description;
	private final List<String> storeDetailImageUrls;

	private StoreHeadDto(Long storeId, String storeName, Boolean isOpened, String headImageUrl, String description,
		List<String> storeDetailImageUrls) {
		this.storeId = storeId;
		this.storeName = storeName;
		this.isOpened = isOpened;
		this.headImageUrl = headImageUrl;
		this.description = description;
		this.storeDetailImageUrls = storeDetailImageUrls != null ? List.copyOf(storeDetailImageUrls) : List.of();
	}

	public static StoreHeadDto of(Long storeId, String storeName, Boolean isOpened, String headImageUrl,
		String description, List<String> storeDetailImageUrls) {
		return new StoreHeadDto(storeId, storeName, isOpened, headImageUrl, description,
			storeDetailImageUrls);
	}
}
