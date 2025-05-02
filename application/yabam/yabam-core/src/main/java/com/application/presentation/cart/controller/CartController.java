package com.application.presentation.cart.controller;

import static com.response.ResponseUtil.*;
import static domain.pos.member.entity.UserRole.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.presentation.cart.api.CartApi;
import com.application.presentation.cart.dto.response.CartInfoResponse;
import com.authorization.AssignUserPassport;
import com.authorization.HasRole;
import com.response.ResponseBody;

import domain.pos.cart.service.CartService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CartController implements CartApi {
	private final CartService cartService;

	@PostMapping("/api/v1/cart")
	@HasRole(userRole = ROLE_USER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<Void>> postCart(
		@RequestParam final Long receiptId,
		@RequestParam final Long menuId,
		@RequestParam final Integer quantity) {
		cartService.upsertCart(receiptId, menuId, quantity);
		return ResponseEntity.ok(createSuccessResponse());
	}

	@DeleteMapping("/api/v1/cart/menu")
	@HasRole(userRole = ROLE_USER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<Void>> deleteCart(
		@RequestParam final Long receiptId,
		@RequestParam final Long menuId) {
		cartService.deleteCartMenu(receiptId, menuId);
		return ResponseEntity.ok(createSuccessResponse());
	}

	@GetMapping("/api/v1/cart")
	@HasRole(userRole = ROLE_USER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<CartInfoResponse>> getCart(
		@RequestParam final Long receiptId) {
		return ResponseEntity.ok(createSuccessResponse(
			cartService.getCart(receiptId)
				.map(CartInfoResponse::from)
				.orElseGet(() -> CartInfoResponse.emptyFrom(receiptId))
		));
	}

}
