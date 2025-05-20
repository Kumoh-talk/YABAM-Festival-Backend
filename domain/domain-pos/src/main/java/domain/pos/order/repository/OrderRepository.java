package domain.pos.order.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.vo.UserRole;

import domain.pos.cart.entity.CartMenu;
import domain.pos.order.entity.Order;
import domain.pos.order.entity.OrderMenu;
import domain.pos.order.entity.vo.OrderStatus;
import domain.pos.receipt.entity.Receipt;

@Repository
public interface OrderRepository {
	Order postOrderWithCart(Receipt receipt, List<CartMenu> cartMenus);

	Order postOrderWithoutCart(Receipt receipt, List<OrderMenu> orderMenus);

	Order postCustomOrder(Receipt receipt, Order order);

	Optional<Order> getOrderWithMenu(Long orderId);

	Optional<Order> getOrderWithStore(Long orderId);

	Optional<Order> getOrderWithStoreAndMenusAndLock(Long orderId);

	Order patchOrderStatus(Order order, OrderStatus orderStatus);

	Order patchCustomOrder(Order previousOrder, Order patchOrder);

	void deleteOrder(Order order);

	Slice<Order> getSaleOrderSliceWithMenuAndTable(Long saleId, List<OrderStatus> orderStatuses, int pageSize,
		Long lastOrderId);

	List<Order> getReceiptOrdersWithMenu(UUID receiptId);

	void retryReceiveOrderStatus(Order order, UserRole userRole);
}
