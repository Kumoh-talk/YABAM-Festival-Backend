package com.event.implement;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.pos.channel.MqChannelHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmitterGenerator {
	private static final long TIMEOUT = 60 * 1000 * 3;
	private final MqChannelHandler mqChannelHandler;

	public SseEmitter setUpSseEmitter(String channelName, Map<Long, SseEmitter> emitterMap, Long storeId) {
		SseEmitter emitter = createEmitter();
		emitter.onTimeout(() -> {
			log.info("[{}] server sent event timed out : key={}", channelName, storeId);
			emitter.complete();
		});
		emitter.onError(e -> {
			log.info("[{}] server sent event error occurred : key={} , e message={}", channelName, storeId,
				e.getMessage());
			emitter.complete();
		});
		emitter.onCompletion(() -> {
			if (emitterMap.remove(storeId) != null) {
				log.info("[{}] server sent event removed in emitter cache: key={}", channelName, storeId);
			}
			log.info("[{}] disconnected by completed server sent event: key={}", channelName, storeId);
			mqChannelHandler.unsubscribe(storeId);
		});
		return emitter;
	}

	private SseEmitter createEmitter() {
		return new SseEmitter(TIMEOUT);
	}
}
