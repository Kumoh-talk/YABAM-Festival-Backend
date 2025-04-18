package domain.pos.order.implement;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import domain.pos.order.entity.Order;
import domain.pos.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderReader {
	private final OrderRepository orderRepository;

	public Optional<Order> getOrder(Long orderId) {
		return orderRepository.getOrder(orderId);
	}

	public Optional<Order> getOrderWithOwner(Long orderId) {
		return orderRepository.getOrderWithOwner(orderId);
	}

	public List<Order> getReceiptOrders(Long receiptId) {
		return orderRepository.getReceiptOrders(receiptId);
	}
}
