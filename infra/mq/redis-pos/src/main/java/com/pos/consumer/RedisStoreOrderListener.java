package com.pos.consumer;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

import com.pos.event.SseChannelProvider;
import com.pos.event.StoreOrderEvent;
import com.pos.util.ChannelPrefixUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisStoreOrderListener implements MessageListener {
	private final SseEventHandler sseEventHandler;
	private final GenericJackson2JsonRedisSerializer serializer;
	private static final String EVENT_NAME = "StoreOrderEvent";

	@Override
	public void onMessage(Message message, byte[] pattern) {
		String channelName = new String(message.getChannel());
		String key = ChannelPrefixUtil.removeStoreOrderPrefix(channelName);
		StoreOrderEvent storeOrderEvent;
		storeOrderEvent = serializer.deserialize(message.getBody(), StoreOrderEvent.class);
		sseEventHandler.handleEventWithSSE(SseChannelProvider.OWNER_STORE, EVENT_NAME, key, storeOrderEvent);
	}
}
