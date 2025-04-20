package domain.pos.order.repository;

import java.util.Optional;

import domain.pos.order.entity.OrderMenu;

public interface OrderMenuRepository {
	OrderMenu postOrderMenu(Long orderId, OrderMenu orderMenu);

	Optional<OrderMenu> getOrderMenuWithStore(Long orderMenuId);

	OrderMenu patchOrderMenuQuantity(OrderMenu orderMenu, int quantity);

	void deleteOrderMenu(Long orderMenuId);
}
