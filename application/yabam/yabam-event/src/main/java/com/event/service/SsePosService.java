package com.event.service;

import java.util.Map;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.event.channel.SseChannel;
import com.event.implement.OwnerStoreValidator;
import com.pos.consumer.SseEventHandler;
import com.pos.event.SseChannelProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SsePosService implements SseEventHandler {
	private final OwnerStoreValidator ownerStoreValidator;
	private final Map<SseChannelProvider, SseChannel> sseChannelMap;

	public SseEmitter subscribeByOwner(Long ownerId, Long storeId) {
		ownerStoreValidator.validate(ownerId, storeId);
		SseChannel sseChannel = sseChannelMap.get(SseChannelProvider.OWNER_STORE);
		SseEmitter emitter = sseChannel.subscribe(storeId);
		return emitter;
	}

	@Override
	public void handleEventWithSSE(SseChannelProvider sseChannelProvider, String eventName, String key,
		Object eventData) {
		unicast(sseChannelProvider, eventName, Long.parseLong(key), eventData);
	}

	private void unicast(SseChannelProvider sseChannelProvider, String eventName, Long storeId,
		Object eventData) {
		sseChannelMap.get(sseChannelProvider).unicast(eventName, storeId, eventData);
	}
}
