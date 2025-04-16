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

	public Optional<OrderMenu> getOrderMenuWithCustomerAndOwner(Long orderMenuId) {
		return orderMenuRepository.getOrderMenuWithCustomerAndOwner(orderMenuId);
	}

}
