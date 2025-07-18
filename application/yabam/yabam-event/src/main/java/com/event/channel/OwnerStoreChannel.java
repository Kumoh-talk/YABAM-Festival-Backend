package com.event.channel;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.event.implement.EmitterGenerator;
import com.pos.channel.MqChannelHandler;
import com.pos.event.SseChannelProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class OwnerStoreChannel implements SseChannel {
	private final Map<Long, SseEmitter> emitterMap = new ConcurrentHashMap<>();
	private static final long RECONNECTION_TIMEOUT = 1000L;
	private static final long HEARTBEAT_INTERVAL = 10000L;  // 10 s
	private static final SseChannelProvider CHANNEL = SseChannelProvider.OWNER_STORE;
	private final EmitterGenerator emitterGenerator;
	private final MqChannelHandler mqChannelHandler;
	private final ScheduledExecutorService heartbeatPool =
		Executors.newSingleThreadScheduledExecutor(r -> {
			Thread thread = new Thread(r, "sse-heartbeat");
			thread.setDaemon(true);
			return thread;
		});

	public SseEmitter subscribe(Long storeId) {
		SseEmitter emitter = emitterGenerator.setUpSseEmitter(CHANNEL.getChannelName(), emitterMap, storeId);
		emitterMap.put(storeId, emitter);
		mqChannelHandler.subscribe(storeId);
		sendSseAck(storeId, emitter);

		heartbeatPool.scheduleAtFixedRate(() ->
				sendHeartbeat(storeId), HEARTBEAT_INTERVAL,
			HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
		return emitter;
	}

	private static void sendSseAck(Long storeId, SseEmitter emitter) {
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
	}

	private void sendHeartbeat(Long storeId) {
		SseEmitter emitter = emitterMap.get(storeId);
		if (emitter == null) {
			return; // 이미 끊김
		}
		try {
			emitter.send(SseEmitter.event().name("heartbeat").comment(""));
		} catch (IOException ex) {
			log.info("[{}] heartbeat failed -> closing emitter, id={}",
				CHANNEL.getChannelName(), storeId);
			closeEmitter(storeId, emitter);
		}
	}

	private void closeEmitter(Long storeId, SseEmitter emitter) {
		emitter.complete();
		emitterMap.remove(storeId);
		mqChannelHandler.unsubscribe(storeId);
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
