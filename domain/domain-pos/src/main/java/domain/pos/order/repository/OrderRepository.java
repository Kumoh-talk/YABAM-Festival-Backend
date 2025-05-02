package domain.pos.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import domain.pos.order.entity.Order;
import domain.pos.order.entity.OrderMenu;
import domain.pos.order.entity.vo.OrderStatus;

@Repository
public interface OrderRepository {
	// TODO : 인프라 단에서 모듣 메뉴 금액 계산 후 엔티티 저장 시 반영해야함 + orderMenu에 조회한 Menu 엔티티를 직접 넣어야함(응답에 포함되어야하므로)
	Order postOrder(Long receiptId, List<OrderMenu> orderMenus);

	Optional<Order> getOrder(Long orderId);

	Optional<Order> getOrderWithStore(Long orderId);

	Order patchOrderStatus(Order order, OrderStatus orderStatus);

	List<Order> getReceiptOrders(Long receiptId);
}
