package com.pos.global.config;

import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.pos.producer.RedisOrderProducer;

import lombok.RequiredArgsConstructor;

@Configuration
@Import({RedisAutoConfiguration.class,
	RedisOrderProducer.class, RedisSerializerConfig.class})
@RequiredArgsConstructor
public class RedisProducerConfig {

	@Bean
	public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
		ReactiveRedisConnectionFactory reactiveRedisConnectionFactory,
		GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer) {
		RedisSerializationContext<String, Object> context =
			RedisSerializationContext
				.<String, Object>newSerializationContext(RedisSerializer.json())
				.value(genericJackson2JsonRedisSerializer)
				.hashValue(genericJackson2JsonRedisSerializer)
				.build();
		ReactiveRedisTemplate<String, Object> template = new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory,
			context);
		return template;
	}
}
