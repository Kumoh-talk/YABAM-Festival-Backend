package domain.pos.order.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserPassport;
import domain.pos.member.entity.UserRole;
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
import domain.pos.receipt.implement.ReceiptValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderMenuService {
	private final ReceiptValidator receiptValidator;
	private final OrderReader orderReader;
	private final MenuReader menuReader;
	private final OrderMenuReader orderMenuReader;
	private final OrderMenuWriter orderMenuWriter;

	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public OrderMenu patchOrderMenuStatus(Long orderMenuId, UserPassport userPassport,
		OrderMenuStatus orderMenuStatus) {
		OrderMenu orderMenu = orderMenuReader.getOrderMenuWithOrderAndStore(orderMenuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.ORDER_MENU_NOT_FOUND));
		validateOrderStatus(orderMenu.getOrder());
		validateIsOwner(orderMenu, userPassport);

		OrderMenu patchOrderMenu = orderMenuWriter.patchOrderMenuStatus(orderMenu, orderMenuStatus);

		if (orderMenuStatus == OrderMenuStatus.CANCELED || orderMenuStatus == OrderMenuStatus.COMPLETED) {
			eventPublisher.publishEvent(OrderMenuStatusChangedEvent.from(orderMenu.getOrder()));
		}
		return patchOrderMenu;
	}

	@Transactional
	public OrderMenu postOrderMenu(Long orderId, UserPassport userPassport, Long menuId, Integer quantity) {
		Order order = orderReader.getOrderWithStore(orderId)
			.orElseThrow(() -> new ServiceException(ErrorCode.ORDER_NOT_FOUND));
		receiptValidator.validateIsOwner(order.getReceipt(), userPassport);
		MenuInfo menuInfo = menuReader.getMenuInfo(order.getReceipt().getSale().getStore().getStoreId(), menuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));
		return orderMenuWriter.postOrderMenu(menuInfo, quantity, order);
	}

	@Transactional
	public void deleteOrderMenu(Long orderMenuId, UserPassport userPassport) {
		OrderMenu orderMenu = orderMenuReader.getOrderMenuWithOrderAndStore(orderMenuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.ORDER_MENU_NOT_FOUND));
		validateIsOwner(orderMenu, userPassport);
		orderMenuWriter.deleteOrderMenu(orderMenu);
	}

	private void validateIsOwner(OrderMenu orderMenu, UserPassport userPassport) {
		if (!orderMenu.getMenu().getStore().getOwnerPassport().getUserId().equals(userPassport.getUserId())
			|| userPassport.getUserRole() != UserRole.ROLE_OWNER) {
			throw new ServiceException(ErrorCode.ORDER_MENU_ACCESS_DENIED);
		}
	}

	private void validateOrderStatus(Order order) {
		if (order.getOrderStatus() != OrderStatus.RECEIVED) {
			throw new ServiceException(ErrorCode.ORDER_STATUS_NOT_RECEIVED);
		}
	}
}
