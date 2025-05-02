package domain.pos.order.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserPassport;
import domain.pos.member.entity.UserRole;
import domain.pos.menu.implement.MenuReader;
import domain.pos.order.entity.Order;
import domain.pos.order.entity.OrderMenu;
import domain.pos.order.entity.vo.OrderStatus;
import domain.pos.order.implement.OrderReader;
import domain.pos.order.implement.OrderWriter;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.implement.ReceiptCustomerWriter;
import domain.pos.receipt.implement.ReceiptReader;
import domain.pos.receipt.implement.ReceiptValidator;
import domain.pos.store.implement.SaleValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
	private final ReceiptValidator receiptValidator;
	private final SaleValidator saleValidator;
	private final ReceiptCustomerWriter receiptCustomerWriter;
	private final ReceiptReader receiptReader;
	private final MenuReader menuReader;
	private final OrderWriter orderWriter;
	private final OrderReader orderReader;

	// TODO : 위치기반으로 특정 범위 내에 유저가 존재해야만 주문이 가능하도록 구현 필요
	@Transactional
	public Order postOrder(Long receiptId, UserPassport userPassport, List<OrderMenu> orderMenus) {
		List<Receipt> receipts = receiptReader.getNonStopReceiptsWithStoreAndLock(List.of(receiptId));
		if (receipts.isEmpty()) {
			throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
		}
		Receipt receipt = receipts.get(0);

		saleValidator.validateSaleOpen(receipt.getSale());

		Long storeId = receipt.getSale().getStore().getStoreId();
		for (OrderMenu orderMenu : orderMenus) {
			menuReader.getMenuInfo(storeId, orderMenu.getMenu().getMenuInfo().getId())
				.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));
		}

		if (userPassport.getUserRole().isHigherOrEqual(UserRole.ROLE_USER)
			&& !receiptValidator.isStoreOwner(receipt, userPassport)) {
			receiptCustomerWriter.postReceiptCustomer(userPassport.getUserId(), receiptId);
		}
		return orderWriter.postOrder(receiptId, orderMenus);
	}

	@Transactional
	public Order patchOrderStatus(Long orderId, UserPassport userPassport, OrderStatus orderStatus) {
		Order order = orderReader.getOrderWithStore(orderId)
			.orElseThrow(() -> new ServiceException(ErrorCode.ORDER_NOT_FOUND));

		UserRole userRole = receiptValidator.validateRole(order.getReceipt(), userPassport);
		return orderWriter.patchOrderStatus(order, orderStatus, userRole);
	}

	public List<Order> getReceiptOrders(Long receiptId) {
		receiptValidator.validateReceipt(receiptId);
		return orderReader.getReceiptOrders(receiptId);
	}

	public Order getOrder(Long orderId) {
		return orderReader.getOrder(orderId)
			.orElseThrow(() -> new ServiceException(ErrorCode.ORDER_NOT_FOUND));
	}
}
