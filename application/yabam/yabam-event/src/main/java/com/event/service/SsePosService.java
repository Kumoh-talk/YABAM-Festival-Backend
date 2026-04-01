package com.event.service;

import java.util.Map;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.event.channel.SseChannel;
import com.event.implement.OwnerStoreValidator;
import com.pos.consumer.SseEventHandler;
import com.pos.event.SseChannelProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SsePosService implements SseEventHandler {
	private final OwnerStoreValidator ownerStoreValidator;
	private final Map<SseChannelProvider, SseChannel> sseChannelMap;

	public SseEmitter subscribeByOwner(Long ownerId, Long storeId) {
		ownerStoreValidator.validate(ownerId, storeId);
		SseChannel sseChannel = sseChannelMap.get(SseChannelProvider.OWNER_STORE);
		return sseChannel.subscribe(storeId);
	}

	@Override
	public void handleEventWithSSE(SseChannelProvider sseChannelProvider, String eventName, String key,
		Object eventData) {
		sseChannelMap.get(sseChannelProvider).unicast(eventName, Long.parseLong(key), eventData);
	}
}
