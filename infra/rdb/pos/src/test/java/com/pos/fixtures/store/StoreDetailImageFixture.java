package com.pos.fixtures.store;

import java.util.List;

import com.pos.store.entity.StoreDetailImageEntity;
import com.pos.store.entity.StoreEntity;

public class StoreDetailImageFixture {
	private static final String STORE_DETAIL_IMAGE_URL = "https://pos-api.s3.ap-northeast-2.amazonaws.com/store/2023/10/12/0c4f7a8b-1d5e-4a6b-9f2c-0d1e5f7a8b1c.jpg";
	private static final String STORE_DETAIL_IMAGE_URL_2 = "https://pos-api.s3.ap-northeast-2.amazonaws.com/store/2023/10/12/0c4f7a8b-1d5e-4a6b-9f2c-0d1e5f7a8b1d.jpg";
	private static final String STORE_DETAIL_IMAGE_URL_3 = "https://pos-api.s3.ap-northeast-2.amazonaws.com/store/2023/10/12/0c4f7a8b-1d5e-4a6b-9f2c-0d1e5f7a8b1e.jpg";
	private static final String STORE_DETAIL_IMAGE_URL_4 = "https://pos-api.s3.ap-northeast-2.amazonaws.com/store/2023/10/12/0c4f7a8b-1d5e-4a6b-9f2c-0d1e5f7a8b1f.jpg";
	private static final String STORE_DETAIL_IMAGE_URL_5 = "https://pos-api.s3.ap-northeast-2.amazonaws.com/store/2023/10/12/0c4f7a8b-1d5e-4a6b-9f2c-0d1e5f7a8b1g.jpg";

	private static final List<String> STORE_DETAIL_IMAGE_URL_LIST = List.of(
		STORE_DETAIL_IMAGE_URL,
		STORE_DETAIL_IMAGE_URL_2,
		STORE_DETAIL_IMAGE_URL_3
	);

	private static final List<String> STORE_DETAIL_IMAGE_URL_LIST_2 = List.of(
		STORE_DETAIL_IMAGE_URL_4,
		STORE_DETAIL_IMAGE_URL_5
	);

	public static List<StoreDetailImageEntity> CUSTOM_STORE_DETAIL_IMAGES(StoreEntity storeEntity) {
		return STORE_DETAIL_IMAGE_URL_LIST.stream()
			.map(url -> StoreDetailImageEntity.of(
				url,
				storeEntity
			)).toList();
	}
}
