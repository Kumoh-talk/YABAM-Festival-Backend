package com.event.channel;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseChannel {
	SseEmitter subscribe(Long keyId);

	void unicast(String eventName, Long keyId, Object event);
}
