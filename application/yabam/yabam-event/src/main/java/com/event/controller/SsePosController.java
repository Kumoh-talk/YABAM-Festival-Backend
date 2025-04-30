package com.event.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.event.service.SsePosService;
import com.pos.event.SseChannelProvider;
import com.pos.event.StoreOrderEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SsePosController {
	private final SsePosService ssePosService;

	// @HasRole(userRole = domain.pos.member.entity.UserRole.ROLE_OWNER)
	// @AssignUserPassport
	@PostMapping(path = "/api/v1/owner/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public ResponseEntity<SseEmitter> subscribe(
		// TODO : 이거는 나중에 UserPassport 로 바꿔야함
		@RequestParam Long ownerId,
		@RequestParam Long storeId) {
		log.info("SSE Subscribe 요청 ownerId: {} storeId: {}", ownerId, storeId);
		SseEmitter emitter = ssePosService.subscribeByOwner(ownerId, storeId);
		return ResponseEntity.ok(emitter);
	}

	// TODO: 임시 테스트 용
	@PostMapping(path = "/api/v1/owner/unicast")
	public ResponseEntity<Void> unicast(
		@RequestParam Long storeId,
		@RequestBody StoreOrderEvent storeOrderEvent) {
		ssePosService.handleEventWithSSE(SseChannelProvider.OWNER_STORE, "StoreOrderEvent", String.valueOf(storeId),
			storeOrderEvent);
		return ResponseEntity.ok().build();
	}
}
