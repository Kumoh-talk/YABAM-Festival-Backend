package com.application.presentation.cart.controller;

import static com.response.ResponseUtil.*;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.presentation.cart.api.CartApi;
import com.application.presentation.cart.dto.response.CartInfoResponse;
import com.response.ResponseBody;

import domain.pos.cart.service.CartService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CartController implements CartApi {
	private final CartService cartService;

	@PostMapping("/api/v1/cart")
	public ResponseEntity<ResponseBody<Void>> postCart(
		@RequestParam final UUID receiptId,
		@RequestParam final Long menuId,
		@RequestParam @Min(value = 1, message = "수량은 최소 1개 이상입니다.") final Integer quantity) {
		cartService.upsertCart(receiptId, menuId, quantity);
		return ResponseEntity.ok(createSuccessResponse());
	}

	@DeleteMapping("/api/v1/cart/menu")
	public ResponseEntity<ResponseBody<Void>> deleteCart(
		@RequestParam final UUID receiptId,
		@RequestParam final Long menuId) {
		cartService.deleteCartMenu(receiptId, menuId);
		return ResponseEntity.ok(createSuccessResponse());
	}

	@GetMapping("/api/v1/cart")
	public ResponseEntity<ResponseBody<CartInfoResponse>> getCart(
		@RequestParam final UUID receiptId) {
		return ResponseEntity.ok(createSuccessResponse(
			cartService.getCart(receiptId)
				.map(CartInfoResponse::from)
				.orElseGet(() -> CartInfoResponse.emptyFrom(receiptId))
		));
	}

}
