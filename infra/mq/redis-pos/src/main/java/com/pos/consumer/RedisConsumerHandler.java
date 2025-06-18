package com.pos.consumer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisConsumerHandler {
	private final RedisMessageListenerContainer container;
	private final Map<String, MessageListener> listenerMap = new ConcurrentHashMap<>();
	private final RedisStoreOrderListener redisStoreOrderListener;

	public void subscribe(String channel) {
		if (listenerMap.containsKey(channel)) {
			log.info("이미 구독중인 channel {}", channel);
			return;
		}
		listenerMap.put(channel, redisStoreOrderListener);
		container.addMessageListener(redisStoreOrderListener, ChannelTopic.of(channel));
	}

	public void unsubscribe(String channel) {
		if (!listenerMap.containsKey(channel)) {
			log.info("구독중이지 않은 channel {}", channel);
			return;
		}
		container.removeMessageListener(listenerMap.get(channel), ChannelTopic.of(channel));
		listenerMap.remove(channel);
	}
}
