package com.application.presentation.cart.dto.response;

import java.util.List;

import domain.pos.cart.entity.Cart;
import domain.pos.cart.entity.CartMenu;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "CartInfoResponse", description = "장바구니 정보")
@Builder
public record CartInfoResponse(
	@Schema(description = "영수증 ID", example = "1")
	Long receiptId,
	@Schema(description = "장바구니 메뉴 리스트", implementation = CartMenuDto.class)
	List<CartMenuDto> cartMenuDtos
) {
	@Builder
	@Schema(name = "CartMenuDto", description = "장바구니 메뉴 DTO")
	public record CartMenuDto(
		@Schema(description = "수량", example = "1")
		Integer quantity,
		@Schema(description = "메뉴 ID", example = "1")
		Long menuId,
		@Schema(description = "메뉴 순서", example = "1")
		Integer order,
		@Schema(description = "메뉴 이름", example = "아메리카노")
		String name,
		@Schema(description = "메뉴 가격", example = "4500")
		Integer price,
		@Schema(description = "메뉴 설명", example = "깊고 진한 맛의 에스프레소에 물을 더해 부드러운 맛을 느낄 수 있는 커피")
		String description,
		@Schema(description = "메뉴 이미지 URL", example = "https://example.com/images/americano.jpg")
		String imageUrl,
		@Schema(description = "품절 여부", example = "false")
		boolean isSoldOut,
		@Schema(description = "추천 메뉴 여부", example = "true")
		boolean isRecommended
	) {
		public static CartMenuDto from(final CartMenu cartMenu) {
			return CartMenuDto.builder()
				.quantity(cartMenu.getQuantity())
				.menuId(cartMenu.getMenuInfo().getId())
				.order(cartMenu.getMenuInfo().getOrder())
				.name(cartMenu.getMenuInfo().getName())
				.price(cartMenu.getMenuInfo().getPrice())
				.description(cartMenu.getMenuInfo().getDescription())
				.imageUrl(cartMenu.getMenuInfo().getImageUrl())
				.isSoldOut(cartMenu.getMenuInfo().isSoldOut())
				.isRecommended(cartMenu.getMenuInfo().isRecommended())
				.build();
		}
	}

	public static CartInfoResponse from(final Cart cart) {
		return CartInfoResponse.builder()
			.receiptId(cart.getReceiptId())
			.cartMenuDtos(cart.getCartMenus().stream()
				.map(CartMenuDto::from)
				.toList())
			.build();
	}

	public static CartInfoResponse emptyFrom(final Long receiptId) {
		return CartInfoResponse.builder()
			.receiptId(receiptId)
			.cartMenuDtos(List.of())
			.build();
	}
}
