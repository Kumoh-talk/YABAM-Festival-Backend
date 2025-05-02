package com.event.channel;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.event.implement.EmitterGenerator;
import com.pos.event.SseChannelProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class OwnerStoreChannel implements SseChannel {
	private final Map<Long, SseEmitter> emitterMap = new ConcurrentHashMap<>();
	private static final long RECONNECTION_TIMEOUT = 1000L;
	private static final SseChannelProvider CHANNEL = SseChannelProvider.OWNER_STORE;
	private final EmitterGenerator emitterGenerator;

	public SseEmitter subscribe(Long storeId) {
		SseEmitter emitter = emitterGenerator.setUpSseEmitter(CHANNEL.getChannelName(), emitterMap, storeId);
		emitterMap.put(storeId, emitter);
		try {
			SseEmitter.SseEventBuilder event = SseEmitter.event()
				.name(CHANNEL.getChannelName())
				.id(String.valueOf(storeId))
				.data("SSE connected")
				.reconnectTime(RECONNECTION_TIMEOUT);
			emitter.send(event);
		} catch (IOException e) {
			log.warn("[{}] failure send media position data, id={}, {}", CHANNEL.getChannelName(), storeId,
				e.getMessage());
		}
		return emitter;
	}

	public void unicast(String eventName, Long storeId, Object eventData) {
		Optional.ofNullable(emitterMap.get(storeId))
			.ifPresentOrElse(
				emitter -> {
					try {
						emitter.send(SseEmitter.event()
							.name(eventName)
							.id(String.valueOf(storeId))
							.reconnectTime(RECONNECTION_TIMEOUT)
							.data(eventData, MediaType.APPLICATION_JSON));
						log.info("[store-order-event] sent notification, id={}", storeId);
					} catch (IOException e) {
						log.warn("[store-order-event] fail to send emitter id={}, {}", storeId, e.getMessage());
					}
				},
				() -> log.info("[{}] emitter not found, id={}", CHANNEL.getChannelName(), storeId)
			);
	}

}
