package com.pos.order.repository.impl;

import java.time.LocalDateTime;
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
		// 최초 주문 시 영수증 시작시간 기록
		if (!orderJpaRepository.existsOrderByReceiptId(receipt.getReceiptInfo().getReceiptId())) {
			LocalDateTime startTime = receiptJpaRepository.startReceiptUsage(receipt.getReceiptInfo().getReceiptId());
			receipt.getReceiptInfo().setStartUsageTime(startTime);
		}

		// 주문 총 금액 계산
		int totalPrice = cartMenus.stream()
			.mapToInt(cartMenu -> cartMenu.getMenuInfo().getPrice() * cartMenu.getQuantity())
			.sum();
		OrderEntity orderEntity = OrderMapper.toOrderEntity(OrderStatus.ORDERED, totalPrice, receipt);

		// CartMenu -> OrderMenuEntity 변환
		List<OrderMenuEntity> orderMenuEntities = cartMenus.stream()
			.map(cartMenu -> OrderMenuMapper.toOrderMenuEntity(
				cartMenu,
				orderEntity.getStatus().transferOrderMenuStatus(),
				orderEntity,
				MenuMapper.toMenuEntity(cartMenu.getMenuInfo(), null, null)))
			.toList();
		orderEntity.getOrderMenus().addAll(orderMenuEntities);

		OrderEntity savedOrderEntity = orderJpaRepository.save(orderEntity);

		List<OrderMenu> savedOrderMenus = savedOrderEntity.getOrderMenus().stream()
			.map(orderMenuEntity -> OrderMenuMapper.toOrderMenu(orderMenuEntity, null,
				MenuMapper.toMenu(orderMenuEntity.getMenu(), null, null)))
			.toList();
		return OrderMapper.toOrder(
			savedOrderEntity,
			receipt,
			savedOrderMenus);
	}

	@Override
	@Transactional
	public Order postOrderWithoutCart(Receipt receipt, List<OrderMenu> orderMenus) {
		// 최초 주문 시 영수증 시작시간 기록
		if (!orderJpaRepository.existsOrderByReceiptId(receipt.getReceiptInfo().getReceiptId())) {
			LocalDateTime startTime = receiptJpaRepository.startReceiptUsage(receipt.getReceiptInfo().getReceiptId());
			receipt.getReceiptInfo().setStartUsageTime(startTime);
		}

		// 주문 총 금액 계산
		int totalPrice = orderMenus.stream()
			.mapToInt(orderMenu -> orderMenu.getMenu().getMenuInfo().getPrice() * orderMenu.getQuantity())
			.sum();
		OrderEntity orderEntity = OrderMapper.toOrderEntity(OrderStatus.ORDERED, totalPrice, receipt);

		// orderMenu -> OrderMenuEntity 변환
		List<OrderMenuEntity> orderMenuEntities = orderMenus.stream()
			.map(orderMenu -> OrderMenuMapper.toOrderMenuEntity(
				orderMenu.getMenu().getMenuInfo(),
				orderMenu.getQuantity(),
				orderEntity.getStatus().transferOrderMenuStatus(),
				orderEntity))
			.toList();
		orderEntity.getOrderMenus().addAll(orderMenuEntities);

		OrderEntity savedOrderEntity = orderJpaRepository.save(orderEntity);

		List<OrderMenu> savedOrderMenus = savedOrderEntity.getOrderMenus().stream()
			.map(orderMenuEntity -> OrderMenuMapper.toOrderMenu(orderMenuEntity, null,
				MenuMapper.toMenu(orderMenuEntity.getMenu(), null, null)))
			.toList();
		return OrderMapper.toOrder(
			savedOrderEntity,
			receipt,
			savedOrderMenus);
	}

	@Override
	public Optional<Order> getOrderWithMenu(Long orderId) {
		return orderJpaRepository.findByIdWithMenus(orderId)
			.map(orderEntity -> OrderMapper.toOrder(orderEntity, null, orderEntity.getOrderMenus().stream().map(
				orderMenuEntity -> OrderMenuMapper.toOrderMenu(orderMenuEntity, null,
					MenuMapper.toMenu(orderMenuEntity.getMenu(), null, null))).toList()));
	}

	@Override
	public Optional<Order> getOrderWithStore(Long orderId) {
		return orderJpaRepository.findByIdWithStore(orderId)
			.map(orderEntity -> OrderMapper.toOrder(orderEntity, ReceiptMapper.toReceipt(orderEntity.getReceipt(), null,
					SaleMapper.toSale(orderEntity.getReceipt().getSale(),
						StoreMapper.toStore(orderEntity.getReceipt().getSale().getStore()))),
				null));
	}

	@Override
	public Optional<Order> getOrderWithStoreAndMenusAndLock(Long orderId) {
		return orderJpaRepository.findByIdWithStoreAndMenusAndLock(orderId)
			.map(orderEntity -> OrderMapper.toOrder(
				orderEntity,
				ReceiptMapper.toReceipt(orderEntity.getReceipt(), null,
					SaleMapper.toSale(orderEntity.getReceipt().getSale(),
						StoreMapper.toStore(orderEntity.getReceipt().getSale().getStore()))),
				orderEntity.getOrderMenus().stream()
					.map(orderMenuEntity -> OrderMenuMapper.toOrderMenu(orderMenuEntity, null,
						MenuMapper.toMenu(orderMenuEntity.getMenu(), null, null)))
					.toList()));
	}

	@Override
	@Transactional
	public Order patchOrderStatus(Order order, OrderStatus orderStatus) {
		orderJpaRepository.updateOrderStatus(order.getOrderId(), orderStatus);
		orderMenuJpaRepository.updateOrderMenuStatus(order.getOrderId(), orderStatus.transferOrderMenuStatus());
		order.setOrderStatus(orderStatus);
		return order;
	}

	@Override
	public Slice<Order> getSaleOrderSliceWithMenuAndTable(Long saleId, List<OrderStatus> orderStatuses,
		int pageSize, Long lastOrderId) {
		return orderJpaRepository.findSaleOrdersWithMenuAndTable(saleId, orderStatuses, pageSize, lastOrderId)
			.map(orderEntity -> OrderMapper.toOrder(
				orderEntity,
				ReceiptMapper.toReceipt(
					orderEntity.getReceipt(),
					TableMapper.toTable(orderEntity.getReceipt().getTable(), (Store)null),
					null),
				orderEntity.getOrderMenus().stream()
					.map(orderMenuEntity -> OrderMenuMapper.toOrderMenu(orderMenuEntity, null,
						MenuMapper.toMenu(orderMenuEntity.getMenu(), null, null)))
					.toList()));
	}

	@Override
	public List<Order> getReceiptOrdersWithMenu(UUID receiptId) {
		return orderJpaRepository.findReceiptOrdersWithMenu(receiptId)
			.stream()
			.map(orderEntity -> OrderMapper.toOrder(orderEntity, null,
				orderEntity.getOrderMenus().stream()
					.map(orderMenuEntity -> OrderMenuMapper.toOrderMenu(orderMenuEntity, null,
						MenuMapper.toMenu(orderMenuEntity.getMenu(), null, null)))
					.toList()))
			.toList();
	}
}
