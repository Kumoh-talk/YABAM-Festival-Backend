package com.pos.event;

public enum SseChannelProvider {
	OWNER_STORE("OwnerStoreChannel"),
	TABLE_ORDER("TableOrderChannel");

	private final String channelName;

	SseChannelProvider(String channelName) {
		this.channelName = channelName;
	}

	public String getChannelName() {
		return channelName;
	}

	public static SseChannelProvider from(String channelName) {
		for (SseChannelProvider provider : values()) {
			if (provider.getChannelName().equals(channelName)) {
				return provider;
			}
		}
		throw new IllegalArgumentException("Invalid channel name: " + channelName);
	}
}
