package domain.pos.order.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import domain.pos.menu.entity.MenuInfo;
import domain.pos.order.entity.Order;
import domain.pos.order.entity.OrderMenu;
import domain.pos.order.entity.vo.OrderMenuStatus;

@Repository
public interface OrderMenuRepository {
	OrderMenu postOrderMenu(MenuInfo menuInfo, Integer quantity, Order order);

	Optional<OrderMenu> getOrderMenuWithOrderAndStore(Long orderMenuId);

	void deleteOrderMenu(OrderMenu orderMenu);

	OrderMenu patchOrderMenuStatus(OrderMenu orderMenu, OrderMenuStatus orderMenuStatus);

	boolean existsCookingMenu(Long orderId);
}
