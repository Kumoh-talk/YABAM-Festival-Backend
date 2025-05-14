package com.application.presentation.call.controller;

import static com.response.ResponseUtil.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.presentation.call.api.CallApi;
import com.application.presentation.call.dto.request.CallCreateRequest;
import com.application.presentation.call.dto.response.CallCursorResponse;
import com.authorization.AssignUserPassport;
import com.authorization.HasRole;
import com.response.ResponseBody;
import com.vo.UserPassport;
import com.vo.UserRole;

import domain.pos.call.service.CallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CallController implements CallApi {
	private final CallService callService;

	@PostMapping("/api/v1/call")
	public ResponseEntity<ResponseBody<Void>> postCall(
		@RequestBody @Valid CallCreateRequest callCreateRequest) {
		callService.postCall(callCreateRequest.receiptId(), callCreateRequest.storeId(),
			callCreateRequest.getCallMessage());
		return ResponseEntity.ok(createSuccessResponse());
	}

	@PatchMapping("/api/v1/call/complete")
	@HasRole(userRole = UserRole.ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<Void>> completeCall(
		UserPassport userPassport,
		@RequestParam Long callId) {
		callService.completeCall(userPassport, callId);
		return ResponseEntity.ok(createSuccessResponse());
	}

	@GetMapping("/api/v1/calls")
	@HasRole(userRole = UserRole.ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<CallCursorResponse>> getNonCompleteCalls(
		UserPassport userPassport,
		@RequestParam Long saleId,
		@RequestParam Long lastCallId,
		@RequestParam Integer size) {
		return ResponseEntity.ok(createSuccessResponse(
			CallCursorResponse.from(callService.getNonCompleteCalls(userPassport, saleId, lastCallId, size))));
	}
}
