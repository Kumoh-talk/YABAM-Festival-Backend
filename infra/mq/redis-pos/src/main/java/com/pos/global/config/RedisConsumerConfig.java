package com.pos.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import com.pos.consumer.RedisConsumerHandler;
import com.pos.consumer.RedisStoreOrderListener;

import lombok.RequiredArgsConstructor;

@Configuration
@Import({RedisStoreOrderListener.class, RedisConsumerHandler.class, RedisSerializerConfig.class})
@RequiredArgsConstructor
public class RedisConsumerConfig {
	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		return container;
	}

}
