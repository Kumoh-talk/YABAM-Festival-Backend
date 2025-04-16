package domain.pos.order.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserPassport;
import domain.pos.menu.implement.MenuReader;
import domain.pos.order.entity.Order;
import domain.pos.order.entity.OrderMenu;
import domain.pos.order.entity.vo.OrderStatus;
import domain.pos.order.implement.OrderReader;
import domain.pos.order.implement.OrderWriter;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.implement.ReceiptReader;
import domain.pos.receipt.implement.ReceiptValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
	private final ReceiptValidator receiptValidator;
	private final ReceiptReader receiptReader;
	private final MenuReader menuReader;
	private final OrderWriter orderWriter;
	private final OrderReader orderReader;

	@Transactional
	public Order postOrder(Long receiptId, UserPassport userPassport, List<OrderMenu> orderMenus) {
		Receipt receipt = receiptReader.getReceiptWithCustomerAndOwner(receiptId)
			.orElseThrow(() -> new ServiceException(ErrorCode.RECEIPT_NOT_FOUND));
		receiptValidator.validateAccessToReceipt(receipt, userPassport);

		Long storeId = receipt.getSale().getStore().getStoreId();
		for (OrderMenu orderMenu : orderMenus) {
			menuReader.getMenuInfo(storeId, orderMenu.getMenu().getMenuInfo().getMenuId())
				.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));
		}
		return orderWriter.postOrder(receiptId, orderMenus);
	}

	@Transactional
	public Order patchOrderStatus(Long orderId, UserPassport userPassport, OrderStatus orderStatus) {
		Order order = orderReader.getOrderWithCustomerAndOwner(orderId)
			.orElseThrow(() -> new ServiceException(ErrorCode.ORDER_NOT_FOUND));

		receiptValidator.validateAccessToReceipt(order.getReceipt(), userPassport);
		return orderWriter.patchOrderStatus(order, orderStatus, userPassport.getUserRole());
	}

	public List<Order> getReceiptOrders(Long receiptId, UserPassport userPassport) {
		Receipt receipt = receiptReader.getReceiptWithCustomerAndOwner(receiptId)
			.orElseThrow(() -> new ServiceException(ErrorCode.RECEIPT_NOT_FOUND));
		receiptValidator.validateAccessToReceipt(receipt, userPassport);
		return orderReader.getReceiptOrders(receiptId);
	}

	public Order getOrder(Long orderId, UserPassport userPassport) {
		Order order = orderReader.getOrderWithCustomerAndOwner(orderId)
			.orElseThrow(() -> new ServiceException(ErrorCode.ORDER_NOT_FOUND));
		receiptValidator.validateAccessToReceipt(order.getReceipt(), userPassport);
		return order;
	}
}
