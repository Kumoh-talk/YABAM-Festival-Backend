package com.pos.producer;

import com.pos.event.StoreOrderEvent;

public interface StoreOrderProducer {
	void produceStoreOrder(String key, StoreOrderEvent storeOrderEvent);
}
