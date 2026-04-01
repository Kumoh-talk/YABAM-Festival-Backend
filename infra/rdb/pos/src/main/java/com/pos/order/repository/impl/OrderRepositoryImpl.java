package com.pos.order.repository.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.pos.menu.mapper.MenuMapper;
import com.pos.order.entity.OrderEntity;
import com.pos.order.entity.OrderMenuEntity;
import com.pos.order.mapper.OrderMapper;
import com.pos.order.mapper.OrderMenuMapper;
import com.pos.order.repository.jpa.OrderJpaRepository;
import com.pos.order.repository.jpa.OrderMenuJpaRepository;
import com.pos.receipt.mapper.ReceiptMapper;
import com.pos.receipt.repository.jpa.ReceiptJpaRepository;
import com.pos.sale.mapper.SaleMapper;
import com.pos.store.mapper.StoreMapper;
import com.pos.table.mapper.TableMapper;
import com.vo.UserRole;

import domain.pos.cart.entity.CartMenu;
import domain.pos.order.entity.Order;
import domain.pos.order.entity.OrderMenu;
import domain.pos.order.entity.vo.OrderStatus;
import domain.pos.order.repository.OrderRepository;
import domain.pos.receipt.entity.Receipt;
import domain.pos.store.entity.Store;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
	private final OrderJpaRepository orderJpaRepository;
	private final OrderMenuJpaRepository orderMenuJpaRepository;
	private final ReceiptJpaRepository receiptJpaRepository;

	@Override
	@Transactional
	public Order postOrderWithCart(Receipt receipt, List<CartMenu> cartMenus) {
		recordFirstOrderStartTime(receipt);

		int totalPrice = cartMenus.stream()
			.mapToInt(cartMenu -> cartMenu.getMenuInfo().getPrice() * cartMenu.getQuantity())
			.sum();
		OrderEntity orderEntity = OrderMapper.toOrderEntity(OrderStatus.ORDERED, totalPrice, receipt);

		List<OrderMenuEntity> orderMenuEntities = cartMenus.stream()
			.map(cartMenu -> OrderMenuMapper.toOrderMenuEntity(
				cartMenu,
				orderEntity.getStatus().transferOrderMenuStatus(),
				orderEntity,
				MenuMapper.toMenuEntity(cartMenu.getMenuInfo(), null, null)))
			.toList();
		orderEntity.getOrderMenus().addAll(orderMenuEntities);

		OrderEntity savedOrderEntity = orderJpaRepository.save(orderEntity);
		return OrderMapper.toOrder(savedOrderEntity, receipt, toOrderMenus(savedOrderEntity));
	}

	@Override
	@Transactional
	public Order postOrderWithoutCart(Receipt receipt, List<OrderMenu> orderMenus) {
		recordFirstOrderStartTime(receipt);

		int totalPrice = orderMenus.stream()
			.mapToInt(orderMenu -> orderMenu.getMenu().getMenuInfo().getPrice() * orderMenu.getQuantity())
			.sum();
		OrderEntity orderEntity = OrderMapper.toOrderEntity(OrderStatus.ORDERED, totalPrice, receipt);

		List<OrderMenuEntity> orderMenuEntities = orderMenus.stream()
			.map(orderMenu -> OrderMenuMapper.toOrderMenuEntity(
				orderMenu.getMenu().getMenuInfo(),
				orderMenu.getQuantity(),
				orderEntity.getStatus().transferOrderMenuStatus(),
				orderEntity))
			.toList();
		orderEntity.getOrderMenus().addAll(orderMenuEntities);

		OrderEntity savedOrderEntity = orderJpaRepository.save(orderEntity);
		return OrderMapper.toOrder(savedOrderEntity, receipt, toOrderMenus(savedOrderEntity));
	}

	@Override
	public Order postCustomOrder(Receipt receipt, Order order) {
		recordFirstOrderStartTime(receipt);

		OrderEntity orderEntity = OrderMapper.toOrderEntity(order, receipt);
		OrderEntity savedOrderEntity = orderJpaRepository.save(orderEntity);
		return OrderMapper.toOrder(
			savedOrderEntity,
			receipt,
			new ArrayList<>());
	}

	@Override
	public Optional<Order> getOrderWithMenu(Long orderId) {
		return orderJpaRepository.findByIdWithMenus(orderId)
			.map(orderEntity -> OrderMapper.toOrder(orderEntity, null, toOrderMenus(orderEntity)));
	}

	@Override
	public Optional<Order> getOrderWithStore(Long orderId) {
		return orderJpaRepository.findByIdWithStore(orderId)
			.map(orderEntity -> OrderMapper.toOrder(orderEntity, toReceiptWithStore(orderEntity), null));
	}

	@Override
	@Transactional
	public Optional<Order> getOrderWithStoreAndMenusAndLock(Long orderId) {
		return orderJpaRepository.findByIdWithStoreAndMenusAndLock(orderId)
			.map(orderEntity -> OrderMapper.toOrder(orderEntity, toReceiptWithStore(orderEntity),
				toOrderMenus(orderEntity)));
	}

	@Override
	@Transactional
	public Order patchOrderStatus(Order order, OrderStatus orderStatus) {
		orderJpaRepository.updateOrderStatus(order.getOrderId(), orderStatus);
		orderMenuJpaRepository.updateOrderMenuStatusByOrderId(order.getOrderId(),
			orderStatus.transferOrderMenuStatus());
		order.setOrderStatus(orderStatus);
		return order;
	}

	@Override
	public Order patchCustomOrder(Order previousOrder, Order patchOrder) {
		orderJpaRepository.updateCustomOrder(previousOrder.getOrderId(), patchOrder);
		previousOrder.setCustomOrder(patchOrder);
		return previousOrder;
	}

	@Override
	public void deleteOrder(Order order) {
		orderJpaRepository.deleteById(order.getOrderId());
	}

	@Override
	public Slice<Order> getSaleOrderSliceWithMenuAndTable(Long saleId, List<OrderStatus> orderStatuses,
		int pageSize, Long lastOrderId) {
		return orderJpaRepository.findSaleOrdersWithMenuAndTable(saleId, orderStatuses, pageSize, lastOrderId)
			.map(orderEntity -> OrderMapper.toOrder(
				orderEntity,
				ReceiptMapper.toReceipt(orderEntity.getReceipt(),
					TableMapper.toTable(orderEntity.getReceipt().getTable(), (Store)null), null),
				toOrderMenus(orderEntity)));
	}

	@Override
	public List<Order> getReceiptOrdersWithMenu(UUID receiptId) {
		return orderJpaRepository.findReceiptOrdersWithMenu(receiptId)
			.stream()
			.map(orderEntity -> OrderMapper.toOrder(orderEntity, null, toOrderMenus(orderEntity)))
			.toList();
	}

	@Override
	public void retryReceiveOrderStatus(Order order, UserRole userRole) {
		orderJpaRepository.updateOrderStatus(order.getOrderId(), OrderStatus.RECEIVED);
	}

	private void recordFirstOrderStartTime(Receipt receipt) {
		if (!orderJpaRepository.existsOrderByReceiptId(receipt.getReceiptInfo().getReceiptId())) {
			LocalDateTime startTime = receiptJpaRepository.startReceiptUsage(
				receipt.getReceiptInfo().getReceiptId());
			receipt.getReceiptInfo().setStartUsageTime(startTime);
		}
	}

	private static List<OrderMenu> toOrderMenus(OrderEntity orderEntity) {
		return orderEntity.getOrderMenus().stream()
			.map(orderMenuEntity -> OrderMenuMapper.toOrderMenu(orderMenuEntity, null,
				MenuMapper.toMenu(orderMenuEntity.getMenu(), null, null)))
			.toList();
	}

	private static Receipt toReceiptWithStore(OrderEntity orderEntity) {
		return ReceiptMapper.toReceipt(orderEntity.getReceipt(), null,
			SaleMapper.toSale(orderEntity.getReceipt().getSale(),
				StoreMapper.toStore(orderEntity.getReceipt().getSale().getStore())));
	}
}
