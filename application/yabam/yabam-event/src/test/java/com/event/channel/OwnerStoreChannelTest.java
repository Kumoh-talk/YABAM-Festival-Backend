package com.event.channel;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.event.implement.EmitterGenerator;
import com.pos.channel.MqChannelHandler;
import com.pos.event.SseChannelProvider;

@ExtendWith(MockitoExtension.class)
class OwnerStoreChannelTest {

	@Mock
	private EmitterGenerator emitterGenerator;

	@Mock
	private MqChannelHandler mqChannelHandler;

	@Mock
	private SseEmitter mockEmitter;

	private OwnerStoreChannel ownerStoreChannel;

	@BeforeEach
	void setUp() {
		ownerStoreChannel = new OwnerStoreChannel(emitterGenerator, mqChannelHandler);
	}

	@Test
	void unicast_등록된_emitter_없으면_예외_없이_종료() {
		assertThatNoException().isThrownBy(() ->
			ownerStoreChannel.unicast("orderEvent", 1L, "payload"));
	}

	@Test
	void unicast_등록된_emitter에_이벤트_전송() throws IOException {
		when(emitterGenerator.setUpSseEmitter(anyString(), any(), eq(1L)))
			.thenReturn(mockEmitter);
		doNothing().when(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));

		ownerStoreChannel.subscribe(1L);
		ownerStoreChannel.unicast("orderEvent", 1L, "payload");

		// subscribe ack + unicast send = 2 calls
		verify(mockEmitter, times(2)).send(any(SseEmitter.SseEventBuilder.class));
	}

	@Test
	void unicast_IOException_발생시_예외_전파하지_않음() throws IOException {
		when(emitterGenerator.setUpSseEmitter(anyString(), any(), eq(1L)))
			.thenReturn(mockEmitter);
		doNothing().doThrow(IOException.class).when(mockEmitter)
			.send(any(SseEmitter.SseEventBuilder.class));

		ownerStoreChannel.subscribe(1L);

		assertThatNoException().isThrownBy(() ->
			ownerStoreChannel.unicast("orderEvent", 1L, "payload"));
	}

	@Test
	void unicast_다른_storeId는_영향_없음() throws IOException {
		when(emitterGenerator.setUpSseEmitter(anyString(), any(), eq(1L)))
			.thenReturn(mockEmitter);
		doNothing().when(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));

		ownerStoreChannel.subscribe(1L);

		// storeId 2는 emitter 없음 → 예외 없이 무시
		assertThatNoException().isThrownBy(() ->
			ownerStoreChannel.unicast("orderEvent", 2L, "payload"));

		// storeId 1만 subscribe 시 ack 1회 전송
		verify(mockEmitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
	}
}
