package com.pos.channel;

public interface MqChannelHandler {
	void subscribe(Long id);

	void unsubscribe(Long id);
}
