package com.pos.consumer;

import org.springframework.stereotype.Component;

import com.pos.event.StoreOrderEvent;

@Component
public interface StoreOrderHandler {
	void handleStoreOrder(String key, StoreOrderEvent storeOrderEvent);
}
