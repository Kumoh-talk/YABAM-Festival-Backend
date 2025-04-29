package com.event.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.event.channel.OwnerStoreChannel;
import com.event.implement.OwnerStoreValidator;
import com.pos.consumer.StoreOrderHandler;
import com.pos.event.StoreOrderEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SsePosService implements StoreOrderHandler {
	private final OwnerStoreChannel ownerStoreChannel;
	private final OwnerStoreValidator ownerStoreValidator;

	public SseEmitter subscribeByOwner(Long ownerId, Long storeId) {
		ownerStoreValidator.validate(ownerId, storeId);
		SseEmitter emitter = ownerStoreChannel.subscribe(storeId);
		return emitter;
	}

	public void unicast(Long storeId, StoreOrderEvent storeOrderEvent) {
		ownerStoreChannel.unicast("StoreOrderEvent", storeId, storeOrderEvent);
	}

	@Override
	public void handleStoreOrder(String eventName, String key, StoreOrderEvent storeOrderEvent) {
		ownerStoreChannel.unicast(eventName, Long.parseLong(key), storeOrderEvent);
	}
}
