package com.pos.consumer;

import org.springframework.stereotype.Component;

import com.pos.event.SseChannelProvider;

@Component
public interface SseEventHandler {
	void handleEventWithSSE(SseChannelProvider sseChannelProvider, String eventName, String key,
		Object eventData);
}
