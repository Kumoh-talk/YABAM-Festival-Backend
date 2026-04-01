package fixtures.store;

import domain.pos.store.entity.StoreInfo;

public class StoreInfoFixture {
	private static final String STORE_NAME = "야밤 주막";
	private static final String STORE_NAME_2 = "금오 주막";
	private static final String STORE_LOCATION = "37.123456, 127.123456";
	private static final String STORE_LOCATION_2 = "42.123456, 127.113676";
	private static final String STORE_DESCRIPTION = "야밤 주막은 맛있는 음식을 제공합니다.";
	private static final String STORE_DESCRIPTION_2 = "금오 주막은 맛있는 음식을 제공합니다.";
	private static final String STORE_IMAGE_URL = "https://www.yabam.com/store.jpg";
	private static final String STORE_IMAGE_URL_2 = "https://www.geumoh.com/store.jpg";
	private static final String STORE_UNIVERSITY = "금오공과대학교";
	private static final String STORE_UNIVERSITY_2 = "서울대학교";
	private static final Integer STORE_TABLE_TIME = 30;
	private static final Integer STORE_TABLE_TIME_2 = 60;
	private static final Integer STORE_TABLE_COST = 10000;
	private static final Integer STORE_TABLE_COST_2 = 20000;

	public static StoreInfo GENERAL_STORE_INFO() {
		return StoreInfo.of(
			STORE_NAME,
			STORE_LOCATION,
			STORE_DESCRIPTION,
			STORE_IMAGE_URL,
			STORE_UNIVERSITY,
			STORE_TABLE_TIME,
			STORE_TABLE_COST
		);
	}

	public static StoreInfo CHANGED_GENERAL_STORE_INFO() {
		return StoreInfo.of(
			STORE_NAME_2,
			STORE_LOCATION_2,
			STORE_DESCRIPTION_2,
			STORE_IMAGE_URL_2,
			STORE_UNIVERSITY_2,
			STORE_TABLE_TIME_2,
			STORE_TABLE_COST_2
		);
	}
}
