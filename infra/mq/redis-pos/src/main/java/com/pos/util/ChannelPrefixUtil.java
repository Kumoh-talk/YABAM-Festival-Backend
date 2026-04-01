package com.pos.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChannelPrefixUtil {
	public static final String STORE_ORDER_PREFIX = "storeId:";

	public static String removeStoreOrderPrefix(String channelName) {
		return channelName.replace(STORE_ORDER_PREFIX, "");
	}
}
