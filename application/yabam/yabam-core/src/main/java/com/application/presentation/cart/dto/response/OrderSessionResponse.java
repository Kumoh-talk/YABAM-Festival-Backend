package com.application.presentation.cart.dto.response;

import java.util.UUID;

import domain.pos.cart.entity.Cart;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "OrderSessionResponse", description = "주문 대기 세션 응답")
public record OrderSessionResponse(
	@Schema(description = "세션 토큰", example = "abc12345-e89b-12d3-a456-426614174000")
	UUID sessionToken,
	@Schema(description = "최신 장바구니 정보", implementation = CartInfoResponse.class)
	CartInfoResponse cart
) {
	public static OrderSessionResponse from(final Cart cart) {
		return new OrderSessionResponse(cart.getSessionToken(), CartInfoResponse.from(cart));
	}
}
