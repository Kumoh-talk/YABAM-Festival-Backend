package com.event.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.event.channel.OwnerStoreChannel;
import com.event.channel.SseChannel;
import com.event.implement.EmitterGenerator;
import com.event.implement.OwnerStoreValidator;
import com.event.service.SsePosService;
import com.pos.event.SseChannelProvider;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SseChannelConfig {
	private final EmitterGenerator emitterGenerator;
	private final OwnerStoreValidator ownerStoreValidator;

	@Bean
	public SseChannel ownerStoreChannel() {
		return new OwnerStoreChannel(emitterGenerator);
	}

	@Bean
	public SsePosService ssePosService() {
		return new SsePosService(ownerStoreValidator, Map.of(
			SseChannelProvider.OWNER_STORE, ownerStoreChannel()
		));
	}
}
