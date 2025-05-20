package com.pos.order.repository.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.pos.menu.mapper.MenuMapper;
import com.pos.order.entity.OrderMenuEntity;
import com.pos.order.mapper.OrderMapper;
import com.pos.order.mapper.OrderMenuMapper;
import com.pos.order.repository.jpa.OrderJpaRepository;
import com.pos.order.repository.jpa.OrderMenuJpaRepository;
import com.pos.store.mapper.StoreMapper;

import domain.pos.menu.entity.Menu;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.order.entity.Order;
import domain.pos.order.entity.OrderMenu;
import domain.pos.order.entity.vo.OrderMenuStatus;
import domain.pos.order.repository.OrderMenuRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderMenuRepositoryImpl implements OrderMenuRepository {
	private final OrderMenuJpaRepository orderMenuJpaRepository;
	private final OrderJpaRepository orderJpaRepository;

	@Override
	@Transactional
	public OrderMenu postOrderMenu(MenuInfo menuInfo, Integer quantity, Order order) {
		OrderMenuEntity orderMenuEntity = OrderMenuMapper.toOrderMenuEntity(menuInfo, quantity, OrderMenuStatus.COOKING,
			order);
		orderJpaRepository.plusOrderPrice(order.getOrderId(), menuInfo.getPrice() * quantity);
		return OrderMenuMapper.toOrderMenu(orderMenuJpaRepository.save(orderMenuEntity), order,
			Menu.of(menuInfo, null, null));
	}

	@Override
	public Optional<OrderMenu> getOrderMenuWithOrderAndStoreAndOrderLock(Long orderMenuId) {
		return orderMenuJpaRepository.findByIdWithOrderAndStoreAndOrderLock(orderMenuId)
			.map(orderMenuEntity -> OrderMenuMapper.toOrderMenu(orderMenuEntity,
				OrderMapper.toOrder(orderMenuEntity.getOrder(), null, null),
				MenuMapper.toMenu(orderMenuEntity.getMenu(),
					StoreMapper.toStore(orderMenuEntity.getMenu().getStore()), null)));
	}

	@Override
	@Transactional
	public void deleteOrderMenu(OrderMenu orderMenu) {
		orderJpaRepository.subtractOrderPrice(orderMenu.getOrder().getOrderId(),
			orderMenu.getMenu().getMenuInfo().getPrice() * orderMenu.getQuantity());
		orderMenuJpaRepository.deleteById(orderMenu.getOrderMenuId());
	}

	@Override
	public OrderMenu patchOrderMenuQuantity(OrderMenu orderMenu, Integer patchQuantity) {
		if (orderMenu.getQuantity().equals(patchQuantity)) {
			return orderMenu;
		} else if (orderMenu.getQuantity() < patchQuantity) {
			int menuPrice = orderMenu.getMenu().getMenuInfo().getPrice() * (patchQuantity - orderMenu.getQuantity());
			orderJpaRepository.plusOrderPrice(orderMenu.getOrder().getOrderId(), menuPrice);
		} else {
			int menuPrice = orderMenu.getMenu().getMenuInfo().getPrice() * (orderMenu.getQuantity() - patchQuantity);
			orderJpaRepository.subtractOrderPrice(orderMenu.getOrder().getOrderId(), menuPrice);
		}

		orderMenuJpaRepository.updateOrderMenuQuantity(orderMenu.getOrderMenuId(), patchQuantity);
		orderMenu.setQuantity(patchQuantity);
		return orderMenu;
	}

	@Override
	public OrderMenu patchOrderMenuCompletedCount(OrderMenu orderMenu, Integer patchCompletedCount) {
		orderMenuJpaRepository.updateOrderMenuCompletedCount(orderMenu.getOrderMenuId(), patchCompletedCount);
		orderMenu.setCompletedCount(patchCompletedCount);
		return orderMenu;
	}

	@Override
	@Transactional
	public OrderMenu patchOrderMenuStatus(OrderMenu orderMenu, OrderMenuStatus orderMenuStatus) {
		if (orderMenuStatus == OrderMenuStatus.CANCELED) {
			int menuPrice = orderMenu.getMenu().getMenuInfo().getPrice() * orderMenu.getQuantity();
			orderJpaRepository.subtractOrderPrice(orderMenu.getOrder().getOrderId(), menuPrice);
		} else if (orderMenu.getOrderMenuStatus() == OrderMenuStatus.CANCELED
			&& orderMenuStatus == OrderMenuStatus.COOKING) {
			int menuPrice = orderMenu.getMenu().getMenuInfo().getPrice() * orderMenu.getQuantity();
			orderJpaRepository.plusOrderPrice(orderMenu.getOrder().getOrderId(), menuPrice);
		}

		orderMenuJpaRepository.updateOrderMenuStatus(orderMenu.getOrderMenuId(), orderMenuStatus);
		orderMenu.setOrderMenuStatus(orderMenuStatus);
		return orderMenu;
	}

	@Override
	public boolean existsCookingMenu(Long orderId) {
		return orderMenuJpaRepository.existsCookingMenu(orderId);
	}
}
