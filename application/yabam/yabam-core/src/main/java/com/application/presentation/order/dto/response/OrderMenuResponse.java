package com.application.presentation.order.dto.response;

import com.application.presentation.menu.dto.response.MenuInfoResponse;

import domain.pos.order.entity.OrderMenu;
import domain.pos.order.entity.vo.OrderMenuStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 메뉴 응답 DTO")
public record OrderMenuResponse(
	@Schema(description = "주문 메뉴 id", example = "1")
	Long orderMenuId,
	@Schema(description = "주문 메뉴 상태", example = "COOKING")
	OrderMenuStatus orderMenuStatus,
	@Schema(description = "주문 메뉴 수량", example = "2")
	Integer quantity,
	@Schema(description = "메뉴 세부정보")
	MenuInfoResponse menuInfo
) {
	public static OrderMenuResponse from(OrderMenu orderMenu) {
		return new OrderMenuResponse(
			orderMenu.getOrderMenuId(),
			orderMenu.getOrderMenuStatus(),
			orderMenu.getQuantity(),
			MenuInfoResponse.from(orderMenu.getMenu().getMenuInfo())
		);
	}
}
