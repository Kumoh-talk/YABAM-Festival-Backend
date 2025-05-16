package domain.pos.order.implement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import domain.pos.order.entity.Order;
import domain.pos.order.entity.vo.OrderStatus;
import domain.pos.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderReader {
	private final OrderRepository orderRepository;

	public Optional<Order> getOrderWithMenu(Long orderId) {
		return orderRepository.getOrderWithMenu(orderId);
	}

	public Optional<Order> getOrderWithStore(Long orderId) {
		return orderRepository.getOrderWithStore(orderId);
	}

	public Optional<Order> getOrderWithStoreAndMenusAndLock(Long orderId) {
		return orderRepository.getOrderWithStoreAndMenusAndLock(orderId);
	}

	public Slice<Order> getSaleOrderSliceWithMenuAndTable(Long saleId, List<OrderStatus> orderStatuses,
		int pageSize, Long lastOrderId) {
		return orderRepository.getSaleOrderSliceWithMenuAndTable(saleId, orderStatuses, pageSize, lastOrderId);
	}

	public List<Order> getReceiptOrdersWithMenu(UUID receiptId) {
		return orderRepository.getReceiptOrdersWithMenu(receiptId);
	}
}
