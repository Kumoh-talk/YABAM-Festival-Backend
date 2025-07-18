package com.pos.util;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ChannelPrefixUtil {
	public static final String STORE_ORDER_PREFIX = "storeId:";

	public static String removeStoreOrderPrefix(String channelName) {
		return channelName.replace(STORE_ORDER_PREFIX, "");
	}
}
