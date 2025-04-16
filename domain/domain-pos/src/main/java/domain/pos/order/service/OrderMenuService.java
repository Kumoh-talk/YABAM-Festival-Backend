package domain.pos.order.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserPassport;
import domain.pos.menu.implement.MenuReader;
import domain.pos.order.entity.Order;
import domain.pos.order.entity.OrderMenu;
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

	@Transactional
	public OrderMenu postOrderMenu(Long orderId, UserPassport userPassport, OrderMenu orderMenu) {
		Order order = orderReader.getOrderWithCustomerAndOwner(orderId)
			.orElseThrow(() -> new ServiceException(ErrorCode.ORDER_NOT_FOUND));
		receiptValidator.validateIsOwner(order.getReceipt(), userPassport);
		menuReader.getMenuInfo(order.getReceipt().getSale().getStore().getStoreId(),
				orderMenu.getMenu().getMenuInfo().getMenuId())
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));
		return orderMenuWriter.postOrderMenu(orderId, orderMenu);
	}

	@Transactional
	public OrderMenu patchOrderMenuQuantity(Long orderMenuId, UserPassport userPassport, Integer quantity) {
		OrderMenu orderMenu = orderMenuReader.getOrderMenuWithCustomerAndOwner(orderMenuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.ORDER_MENU_NOT_FOUND));
		receiptValidator.validateIsOwner(orderMenu.getOrder().getReceipt(), userPassport);
		return orderMenuWriter.patchOrderMenuQuantity(orderMenu, quantity);
	}

	public void deleteOrderMenu(Long orderMenuId, UserPassport userPassport) {
		OrderMenu orderMenu = orderMenuReader.getOrderMenuWithCustomerAndOwner(orderMenuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.ORDER_MENU_NOT_FOUND));
		receiptValidator.validateIsOwner(orderMenu.getOrder().getReceipt(), userPassport);
		orderMenuWriter.deleteOrderMenu(orderMenuId);
	}
}
