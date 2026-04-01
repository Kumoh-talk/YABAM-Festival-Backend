package domain.pos.order.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;
import com.vo.UserRole;

import domain.pos.menu.entity.MenuInfo;
import domain.pos.menu.implement.MenuReader;
import domain.pos.order.entity.Order;
import domain.pos.order.entity.OrderMenu;
import domain.pos.order.entity.vo.OrderMenuStatus;
import domain.pos.order.entity.vo.OrderStatus;
import domain.pos.order.event.OrderMenuStatusChangedEvent;
import domain.pos.order.implement.OrderMenuReader;
import domain.pos.order.implement.OrderMenuWriter;
import domain.pos.order.implement.OrderReader;
import domain.pos.order.implement.OrderWriter;
import domain.pos.receipt.implement.ReceiptValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderMenuService {
	private final ReceiptValidator receiptValidator;
	private final OrderReader orderReader;
	private final OrderWriter orderWriter;
	private final MenuReader menuReader;
	private final OrderMenuReader orderMenuReader;
	private final OrderMenuWriter orderMenuWriter;

	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public OrderMenu patchOrderMenuStatus(Long orderMenuId, UserPassport userPassport,
		OrderMenuStatus orderMenuStatus) {
		OrderMenu orderMenu = orderMenuReader.getOrderMenuWithOrderAndStoreAndOrderLock(orderMenuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.ORDER_MENU_NOT_FOUND));
		Long storeOwnerId = orderMenu.getMenu().getStore().getOwnerPassport().getUserId();
		UserRole userRole = storeOwnerId.equals(userPassport.getUserId())
			&& userPassport.getUserRole() == UserRole.ROLE_OWNER
			? UserRole.ROLE_OWNER : UserRole.ROLE_ANONYMOUS;
		OrderMenu patchOrderMenu = orderMenuWriter.patchOrderMenuStatus(orderMenu, orderMenuStatus, userRole);

		if (orderMenuStatus == OrderMenuStatus.CANCELED || orderMenuStatus == OrderMenuStatus.COMPLETED) {
			eventPublisher.publishEvent(OrderMenuStatusChangedEvent.from(orderMenu.getOrder()));
		}

		if (orderMenuStatus == OrderMenuStatus.COOKING
			&& (orderMenu.getOrder().getOrderStatus() == OrderStatus.COMPLETED
			|| orderMenu.getOrder().getOrderStatus() == OrderStatus.CANCELED)) {
			orderWriter.retryReceiveOrderStatus(orderMenu.getOrder(), userRole);
		}
		return patchOrderMenu;
	}

	@Transactional
	public OrderMenu postOrderMenu(Long orderId, UserPassport userPassport, Long menuId, Integer quantity) {
		Order order = orderReader.getOrderWithStoreAndMenusAndLock(orderId)
			.orElseThrow(() -> new ServiceException(ErrorCode.ORDER_NOT_FOUND));
		if (order.getOrderStatus() != OrderStatus.RECEIVED) {
			throw new ServiceException(ErrorCode.ORDER_STATUS_NOT_RECEIVED);
		}
		receiptValidator.validateIsOwner(order.getReceipt(), userPassport);
		MenuInfo menuInfo = menuReader.getMenuInfo(order.getReceipt().getSale().getStore().getId(), menuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));
		return orderMenuWriter.postOrderMenu(menuInfo, quantity, order);
	}

	@Transactional
	public void deleteOrderMenu(Long orderMenuId, UserPassport userPassport) {
		OrderMenu orderMenu = orderMenuReader.getOrderMenuWithOrderAndStoreAndOrderLock(orderMenuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.ORDER_MENU_NOT_FOUND));
		Order deletingOrder = orderMenu.getOrder();
		if (deletingOrder.getOrderStatus() != OrderStatus.RECEIVED
			&& deletingOrder.getOrderStatus() != OrderStatus.COMPLETED) {
			throw new ServiceException(ErrorCode.ORDER_STATUS_NOT_RECEIVED);
		}
		validateIsOwner(orderMenu, userPassport);
		orderMenuWriter.deleteOrderMenu(orderMenu);
		eventPublisher.publishEvent(OrderMenuStatusChangedEvent.from(orderMenu.getOrder()));
	}

	@Transactional
	public OrderMenu patchOrderMenuQuantity(Long orderMenuId, UserPassport userPassport, Integer quantity) {
		OrderMenu orderMenu = orderMenuReader.getOrderMenuWithOrderAndStoreAndOrderLock(orderMenuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.ORDER_MENU_NOT_FOUND));
		validateOrderMenuStatus(orderMenu);
		validateIsOwner(orderMenu, userPassport);
		return orderMenuWriter.patchOrderMenuQuantity(orderMenu, quantity);
	}

	@Transactional
	public OrderMenu patchOrderMenuCompletedCount(Long orderMenuId, UserPassport userPassport, Integer completedCount) {
		OrderMenu orderMenu = orderMenuReader.getOrderMenuWithOrderAndStoreAndOrderLock(orderMenuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.ORDER_MENU_NOT_FOUND));
		validateOrderMenuStatus(orderMenu);
		validateIsOwner(orderMenu, userPassport);
		if (completedCount > orderMenu.getQuantity()) {
			throw new ServiceException(ErrorCode.COMPLETED_COUNT_OVERFLOW);
		}
		return orderMenuWriter.patchOrderMenuCompletedCount(orderMenu, completedCount);
	}

	private void validateIsOwner(OrderMenu orderMenu, UserPassport userPassport) {
		if (!orderMenu.getMenu().getStore().getOwnerPassport().getUserId().equals(userPassport.getUserId())
			|| userPassport.getUserRole() != UserRole.ROLE_OWNER) {
			throw new ServiceException(ErrorCode.ORDER_MENU_ACCESS_DENIED);
		}
	}

	private void validateOrderMenuStatus(OrderMenu orderMenu) {
		if (orderMenu.getOrderMenuStatus() == OrderMenuStatus.ORDERED
			|| orderMenu.getOrderMenuStatus() == OrderMenuStatus.CANCELED) {
			throw new ServiceException(ErrorCode.ORDER_MENU_STATUS_NOT_ALLOWED);
		}
	}

}
