package fixtures.store;

import java.awt.geom.Point2D;

import domain.pos.store.entity.StoreInfo;

public class StoreInfoFixture {
	// 가게 이름
	private static final String STORE_NAME = "야밤 주막";
	private static final String STORE_NAME_2 = "금오 주막";
	// 가게 위치
	private static final Point2D.Double STORE_LOCATION = new Point2D.Double(37.123456, 127.123456);
	private static final Point2D.Double STORE_LOCATION_2 = new Point2D.Double(42.123456, 127.113676);
	// 가게 상세 설명
	private static final String STORE_DESCRIPTION = "야밤 주막은 맛있는 음식을 제공합니다.";
	private static final String STORE_DESCRIPTION_2 = "금오 주막은 맛있는 음식을 제공합니다.";
	// 가게 이미지 파일 url
	private static final String STORE_IMAGE_URL = "https://www.yabam.com/store.jpg";
	private static final String STORE_IMAGE_URL_2 = "https://www.geumoh.com/store.jpg";
	// 가게 대학교
	private static final String STORE_UNIVERSITY = "금오공과대학교";
	private static final String STORE_UNIVERSITY_2 = "서울대학교";

	public static StoreInfo GENERAL_STORE_INFO() {
		return StoreInfo.of(
			STORE_NAME,
			STORE_LOCATION,
			STORE_DESCRIPTION,
			STORE_IMAGE_URL,
			STORE_UNIVERSITY
		);
	}

	public static StoreInfo CHANGED_GENERAL_STORE_INFO() {
		return StoreInfo.of(
			STORE_NAME_2,
			STORE_LOCATION_2,
			STORE_DESCRIPTION_2,
			STORE_IMAGE_URL_2,
			STORE_UNIVERSITY_2
		);
	}
}
