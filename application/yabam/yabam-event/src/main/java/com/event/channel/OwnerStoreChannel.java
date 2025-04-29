package com.event.channel;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.pos.event.StoreOrderEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OwnerStoreChannel {
	private final Map<Long, SseEmitter> emitterMap = new ConcurrentHashMap<>();
	private static final long TIMEOUT = 60 * 1000 * 3;
	private static final long RECONNECTION_TIMEOUT = 1000L;
	private static final String EVENT_NAME = "OwnerStoreEvent-";

	public SseEmitter subscribe(Long storeId) {
		SseEmitter emitter = setUpSseEmitter(storeId);
		emitterMap.put(storeId, emitter);
		try {
			SseEmitter.SseEventBuilder event = SseEmitter.event()
				.name(EVENT_NAME + String.valueOf(storeId))
				.id(String.valueOf(storeId))
				.data("SSE connected")
				.reconnectTime(RECONNECTION_TIMEOUT);
			emitter.send(event);
		} catch (IOException e) {
			log.warn("[store-order-event] failure send media position data, id={}, {}", storeId, e.getMessage());
		}
		return emitter;
	}

	public void unicast(Long storeId, StoreOrderEvent storeOrderEvent) {
		SseEmitter emitter = emitterMap.get(storeId);
		try {
			emitter.send(SseEmitter.event()
				.name(EVENT_NAME + String.valueOf(storeId))
				.id(String.valueOf(storeId))
				.reconnectTime(RECONNECTION_TIMEOUT)
				.data(storeOrderEvent, MediaType.APPLICATION_JSON));
			log.info("[store-order-event] sended notification, id={}", storeId);
		} catch (IOException e) {
			log.warn("[store-order-event] fail to send emitter id={}, {}", storeId, e.getMessage());
		}
	}

	private SseEmitter setUpSseEmitter(Long storeId) {
		SseEmitter emitter = createEmitter();
		emitter.onTimeout(() -> {
			log.info("[store-order-event] server sent event timed out : id={}", storeId);
			emitter.complete();
		});
		emitter.onError(e -> {
			log.info("[store-order-event] server sent event error occurred : id={}, message={}", storeId,
				e.getMessage());
			emitter.complete();
		});
		emitter.onCompletion(() -> {
			if (emitterMap.remove(storeId) != null) {
				log.info("[store-order-event] server sent event removed in emitter cache: id={}", storeId);
			}
			log.info("[store-order-event] disconnected by completed server sent event: id={}", storeId);
		});
		return emitter;
	}

	private SseEmitter createEmitter() {
		return new SseEmitter(TIMEOUT);
	}
}
