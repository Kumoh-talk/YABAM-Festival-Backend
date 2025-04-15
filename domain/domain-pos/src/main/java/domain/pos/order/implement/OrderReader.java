package domain.pos.order.implement;

import java.util.Optional;

import org.springframework.stereotype.Component;

import domain.pos.order.entity.Order;
import domain.pos.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderReader {
	private final OrderRepository orderRepository;

	public Optional<Order> getOrderWithCustomerAndOwner(Long orderId) {
		return orderRepository.getOrderWithCustomerAndOwner(orderId);
	}
}
