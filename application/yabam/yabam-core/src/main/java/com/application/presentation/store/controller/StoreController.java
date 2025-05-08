package com.application.presentation.store.controller;

import static com.response.ResponseUtil.*;
import static domain.pos.member.entity.UserRole.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.presentation.store.api.StoreApi;
import com.application.presentation.store.dto.request.StoreWriteRequest;
import com.application.presentation.store.dto.response.StoreIdResponse;
import com.application.presentation.store.dto.response.StoreInfoResponse;
import com.authorization.AssignUserPassport;
import com.authorization.HasRole;
import com.response.ResponseBody;

import domain.pos.member.entity.UserPassport;
import domain.pos.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StoreController implements StoreApi {
	private final StoreService storeService;

	@PostMapping("/api/v1/store")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<StoreIdResponse>> createStore(
		UserPassport userPassport,
		@RequestBody @Valid final StoreWriteRequest storeWriteRequest) {
		return ResponseEntity
			.ok(createSuccessResponse(
				StoreIdResponse.of(storeService.createStore(userPassport, storeWriteRequest.toStoreInfo()))
			));
	}

	@GetMapping("/api/v1/store")
	@HasRole(userRole = ROLE_ANONYMOUS)
	public ResponseEntity<ResponseBody<StoreInfoResponse>> getStoreInfo(
		@RequestParam @Valid final Long storeId) {
		return ResponseEntity
			.ok(createSuccessResponse(
				StoreInfoResponse.of(storeService.findStore(storeId))
			));
	}

	@PatchMapping("/api/v1/store")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<StoreIdResponse>> updateStoreInfo(
		UserPassport userPassport,
		@RequestParam final Long storeId,
		@RequestBody @Valid final StoreWriteRequest storeWriteRequest) {
		return ResponseEntity
			.ok(createSuccessResponse(
				StoreIdResponse.from(
					storeService.updateStoreInfo(userPassport, storeId, storeWriteRequest.toStoreInfo()))
			));
	}

	@DeleteMapping("/api/v1/store")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<Void>> deleteStore(
		UserPassport userPassport,
		@RequestParam @Valid final Long storeId) {
		storeService.deleteStore(userPassport, storeId);
		return ResponseEntity
			.ok(createSuccessResponse());
	}

	@PostMapping("/api/v1/store/image")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<Void>> uploadStoreImage(
		UserPassport userPassport,
		@RequestParam @Valid final Long storeId,
		@RequestParam @Valid final String detailImageUrl) {
		storeService.postDetailImage(userPassport, storeId, detailImageUrl);
		return ResponseEntity
			.ok(createSuccessResponse());
	}

	@DeleteMapping("/api/v1/store/image")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<Void>> deleteStoreImage(
		UserPassport userPassport,
		@RequestParam @Valid final Long storeId,
		@RequestParam @Valid final String detailImageUrl) {
		storeService.deleteDetailImage(userPassport, storeId, detailImageUrl);
		return ResponseEntity
			.ok(createSuccessResponse());
	}
}
