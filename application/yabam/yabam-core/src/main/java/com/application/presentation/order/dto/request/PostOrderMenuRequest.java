package com.application.presentation.order.dto.request;

import domain.pos.menu.entity.Menu;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.order.entity.OrderMenu;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "주문 메뉴 생성 요청 DTO")
public record PostOrderMenuRequest(
	@Schema(description = "메뉴 수량", example = "2")
	@NotNull(message = "메뉴 수량은 필수입니다.")
	Integer menuQuantity,

	@Schema(description = "메뉴 id", example = "2")
	@NotNull(message = "메뉴 id는 필수입니다.")
	Long menuId

) {
	public OrderMenu toOrderMenu() {
		return OrderMenu.builder()
			.quantity(menuQuantity)
			.menu(Menu.of(MenuInfo.builder()
				.id(menuId)
				.build(), null, null))
			.build();
	}
}
