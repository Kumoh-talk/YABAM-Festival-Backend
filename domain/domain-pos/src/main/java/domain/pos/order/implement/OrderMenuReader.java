package domain.pos.order.implement;

import java.util.Optional;

import org.springframework.stereotype.Component;

import domain.pos.order.entity.OrderMenu;
import domain.pos.order.repository.OrderMenuRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderMenuReader {
	private final OrderMenuRepository orderMenuRepository;

	public Optional<OrderMenu> getOrderMenuWithOrderAndStoreAndOrderLock(Long orderMenuId) {
		return orderMenuRepository.getOrderMenuWithOrderAndStoreAndOrderLock(orderMenuId);
	}

	public boolean existsCookingMenu(Long orderId) {
		return orderMenuRepository.existsCookingMenu(orderId);
	}

}
