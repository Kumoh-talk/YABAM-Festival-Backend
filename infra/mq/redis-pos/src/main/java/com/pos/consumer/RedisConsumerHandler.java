package com.pos.consumer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import com.pos.channel.MqChannelHandler;
import com.pos.util.ChannelPrefixUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisConsumerHandler implements MqChannelHandler {
	private final RedisMessageListenerContainer container;
	private final Map<String, MessageListener> listenerMap = new ConcurrentHashMap<>();
	private final RedisStoreOrderListener redisStoreOrderListener;

	public void subscribe(Long id) {
		String channel = ChannelPrefixUtil.STORE_ORDER_PREFIX + id;
		if (listenerMap.containsKey(channel)) {
			log.info("이미 구독중인 channel {}", channel);
			return;
		}
		listenerMap.put(channel, redisStoreOrderListener);
		container.addMessageListener(redisStoreOrderListener, ChannelTopic.of(channel));
	}

	public void unsubscribe(Long id) {
		String channel = ChannelPrefixUtil.STORE_ORDER_PREFIX + id;
		if (!listenerMap.containsKey(channel)) {
			log.info("구독중이지 않은 channel {}", channel);
			return;
		}
		container.removeMessageListener(listenerMap.get(channel), ChannelTopic.of(channel));
		listenerMap.remove(channel);
	}
}
