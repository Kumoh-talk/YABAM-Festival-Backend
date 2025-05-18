package domain.pos.order.implement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserRole;

import domain.pos.cart.entity.CartMenu;
import domain.pos.order.entity.Order;
import domain.pos.order.entity.OrderMenu;
import domain.pos.order.entity.vo.OrderStatus;
import domain.pos.order.repository.OrderRepository;
import domain.pos.receipt.entity.Receipt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderWriter {
	private final OrderRepository orderRepository;

	public Order postOrderWithCart(Receipt receipt, List<CartMenu> cartMenus) {
		List<CartMenu> mergedCartMenus = mergeCartMenus(cartMenus);
		return orderRepository.postOrderWithCart(receipt, mergedCartMenus);
	}

	public Order postOrderWithoutCart(Receipt receipt, List<OrderMenu> orderMenus) {
		return orderRepository.postOrderWithoutCart(receipt, orderMenus);
	}

	public Order patchOrderStatus(Order order, OrderStatus orderStatus, UserRole userRole) {
		if (orderStatus == OrderStatus.RECEIVED) {
			order.getOrderStatus().receiveOrder(order.getOrderId(), userRole);
		} else if (orderStatus == OrderStatus.CANCELED) {
			order.getOrderStatus().cancelOrder(order.getOrderId(), userRole);
		} else {
			log.warn("올바른 변경 요청이 아닙니다 : orderStatus={}", orderStatus);
			throw new ServiceException(ErrorCode.INVALID_STATE_TRANSITION);
		}

		return orderRepository.patchOrderStatus(order, orderStatus);
	}

	public void completeOrder(Order order) {
		order.setOrderStatus(OrderStatus.COMPLETED);
		orderRepository.patchOrderStatus(order, OrderStatus.COMPLETED);
	}

	public void retryReceiveOrderStatus(Order order, UserRole userRole) {
		order.getOrderStatus().receiveOrder(order.getOrderId(), userRole);
		orderRepository.retryReceiveOrderStatus(order, userRole);
	}

	private List<CartMenu> mergeCartMenus(List<CartMenu> cartMenus) {
		Map<Long, CartMenu> mergedMap = new HashMap<>();

		for (CartMenu cartMenu : cartMenus) {
			Long menuId = cartMenu.getMenuInfo().getId();
			if (mergedMap.containsKey(menuId)) {
				CartMenu existing = mergedMap.get(menuId);
				existing.addQuantity(cartMenu.getQuantity());
			} else {
				mergedMap.put(menuId, cartMenu);
			}
		}

		return new ArrayList<>(mergedMap.values());
	}
}
