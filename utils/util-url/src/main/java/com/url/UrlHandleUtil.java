package com.url;

import java.util.UUID;

public class UrlHandleUtil {
	private static final String IMAGE_PATH_FORMAT = "/%s/%s/%s";
	private static final String STORE_HEAD_DOMAIN_NAME = "store_head_image";
	private static final String STORE_DETAIL_DOMAIN_NAME = "store_detail_image";
	private static final String STORE_MENU_DOMAIN_NAME = "store_menu_image";

	private UrlHandleUtil() {
	}

	/**
	 * Store 대표 이미지 S3 경로 생성.
	 */
	public static String generateHeadUrl(Long storeId) {
		return String.format(IMAGE_PATH_FORMAT, STORE_HEAD_DOMAIN_NAME, storeId, createUUID());
	}

	/**
	 * Store 상세 이미지 S3 경로 생성.
	 */
	public static String generateDetailUrl(Long storeId) {
		return String.format(IMAGE_PATH_FORMAT, STORE_DETAIL_DOMAIN_NAME, storeId, createUUID());
	}

	public static String generateStoreMenuUrl(Long storeId) {
		return String.format(IMAGE_PATH_FORMAT, STORE_MENU_DOMAIN_NAME, storeId, createUUID());
	}

	private static String createUUID() {
		return UUID.randomUUID().toString();
	}
}
