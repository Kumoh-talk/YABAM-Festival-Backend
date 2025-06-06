package fixtures.store;

import static fixtures.member.UserFixture.*;
import static fixtures.store.StoreInfoFixture.*;

import com.vo.UserPassport;

import java.util.List;

import domain.pos.store.entity.Store;
import domain.pos.store.entity.StoreInfo;

public class StoreFixture {
	// 가게 고유 ID
	private static final Long STORE_ID = 1L;

	// 가게 활성화 여부
	private static final Boolean IS_OPEN = true;
	private static final Boolean IS_CLOSED = false;

	// 가게 점주
	private static final UserPassport GENERAL_STORE_OWNER = OWNER_USER_PASSPORT();

	// 가게 정보
	private static final StoreInfo GENERAL_STORE_INFO = GENERAL_STORE_INFO();
	private static final StoreInfo GENERAL_CHANGED_STORE_INFO = CHANGED_GENERAL_STORE_INFO();

	// 가게 상세 이미지 파일 url
	private static final String STORE_DETAIL_IMAGE_URL = "https://www.yabam.com/store_detail.jpg";
	private static final String STORE_DETAIL_IMAGE_URL_2 = "https://www.geumoh.com/store_detail.jpg";
	private static final List<String> STORE_DETAIL_IMAGE_URL_LIST = List.of(
		STORE_DETAIL_IMAGE_URL,
		STORE_DETAIL_IMAGE_URL_2
	);
	private static final List<String> STORE_DETAIL_IMAGE_URL_LIST_2 = List.of(
		STORE_DETAIL_IMAGE_URL
	);

	public static Store GENERAL_CLOSE_STORE() {
		return Store.of(
			STORE_ID,
			IS_CLOSED,
			GENERAL_STORE_INFO,
			GENERAL_STORE_OWNER,
			STORE_DETAIL_IMAGE_URL_LIST
		);
	}

	public static Store CHANGED_GENERAL_STORE() {
		return Store.of(
			STORE_ID,
			IS_CLOSED,
			GENERAL_CHANGED_STORE_INFO,
			GENERAL_STORE_OWNER,
			STORE_DETAIL_IMAGE_URL_LIST
		);
	}

	public static Store GENERAL_OPEN_STORE() {
		return Store.of(
			STORE_ID,
			IS_OPEN,
			GENERAL_STORE_INFO,
			GENERAL_STORE_OWNER,
			STORE_DETAIL_IMAGE_URL_LIST
		);
	}

	public static Store CUSTOM_STORE(Long storeId, StoreInfo storeInfo, UserPassport ownerPassport) {
		return Store.of(
			storeId,
			IS_CLOSED,
			storeInfo,
			ownerPassport,
			STORE_DETAIL_IMAGE_URL_LIST
		);
	}
}
