package com.url;

import java.util.UUID;

public class UrlHandleUtil {
	private static final String STORE_HEAD_IMAGE_PATH_FORMAT = "/%s/%s/%s";
	private static final String STORE_DETAIL_IMAGE_PATH_FORMAT = "/%s/%s/%s";
	private static final String STORE_HEAD_DOMAIN_NAME = "store_head_image";
	private static final String STORE_DETAIL_DOMAIN_NAME = "store_detail_image";

	private UrlHandleUtil() {

	}

	/**
	 * 컨벤션에 맞는 경로 설정을 위한 메서드입니다.
	 * Store 에 관한 파일 경로를 생성합니다.
	 * Store 에 대표 이미지를 업로드할 때 사용합니다.
	 * @param storeId
	 * @return Board 관련 S3 경로 문자열
	 */
	public static String generatreHeadUrl(Long storeId) {
		return String.format(STORE_HEAD_IMAGE_PATH_FORMAT,
			STORE_HEAD_DOMAIN_NAME,
			storeId,
			creaeteUUID()
		);
	}

	/**
	 * 컨벤션에 맞는 경로 설정을 위한 메서드입니다.
	 * Store 에 관한 파일 경로를 생성합니다.
	 * Store 에 상세 이미지를 업로드할 때 사용합니다.
	 * @param storeId
	 * @return Board 관련 S3 경로 문자열
	 */
	public static String generatreDetailUrl(Long storeId) {
		return String.format(STORE_DETAIL_IMAGE_PATH_FORMAT,
			STORE_DETAIL_DOMAIN_NAME,
			storeId,
			creaeteUUID()
		);
	}

	private static String creaeteUUID() {
		return UUID.randomUUID().toString();
	}

}
