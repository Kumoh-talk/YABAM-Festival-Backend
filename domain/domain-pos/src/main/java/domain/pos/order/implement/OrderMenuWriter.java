package domain.pos.order.implement;

import org.springframework.stereotype.Component;

import domain.pos.order.entity.OrderMenu;
import domain.pos.order.repository.OrderMenuRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderMenuWriter {
	private final OrderMenuRepository orderMenuRepository;

	public OrderMenu postOrderMenu(Long orderId, OrderMenu orderMenu) {
		return orderMenuRepository.postOrderMenu(orderId, orderMenu);
	}

	public OrderMenu patchOrderMenuQuantity(OrderMenu orderMenu, int quantity) {
		return orderMenuRepository.patchOrderMenuQuantity(orderMenu, quantity);
	}

	public void deleteOrderMenu(Long orderMenuId) {
		orderMenuRepository.deleteOrderMenu(orderMenuId);
	}
}
