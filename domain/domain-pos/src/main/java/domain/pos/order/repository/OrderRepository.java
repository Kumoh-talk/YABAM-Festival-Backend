package domain.pos.order.repository;

import java.util.List;
import java.util.Optional;

import domain.pos.order.entity.Order;
import domain.pos.order.entity.OrderMenu;
import domain.pos.order.entity.vo.OrderStatus;

public interface OrderRepository {
	// TODO : 인프라 단에서 모듣 메뉴 금액 계산 후 엔티티 저장 시 반영해야함
	Order postOrder(Long receiptId, List<OrderMenu> orderMenus);

	Optional<Order> getOrderWithCustomerAndOwner(Long orderId);

	Order patchOrderStatus(Order order, OrderStatus orderStatus);
}
