package domain.pos.order.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;
import com.vo.UserRole;

import domain.pos.cart.entity.Cart;
import domain.pos.cart.entity.CartMenu;
import domain.pos.cart.implement.CartWriter;
import domain.pos.menu.entity.MenuInfo;
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
import domain.pos.store.entity.Sale;
import domain.pos.store.implement.SaleReader;
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
	private final CartWriter cartWriter;
	private final SaleReader saleReader;

	// TODO : 위치기반으로 특정 범위 내에 유저가 존재해야만 주문이 가능하도록 구현 필요
	@Transactional
	public Order postOrderWithCart(Long receiptId, UserPassport userPassport) {
		Receipt receipt = receiptReader.getNonStopReceiptsWithTableAndStoreAndLock(receiptId).orElseThrow(
			() -> new ServiceException(ErrorCode.RECEIPT_NOT_FOUND));

		// 영업 상태 검증
		saleValidator.validateSaleOpen(receipt.getSale());

		// 장바구니 조회 및 장바구니 메뉴 존재 검증
		Cart cart = cartWriter.getCart(receiptId)
			.orElseThrow(() -> new ServiceException(ErrorCode.CART_NOT_FOUND));
		if (cart.getCartMenus().isEmpty()) {
			throw new ServiceException(ErrorCode.CART_EMPTY);
		}

		// 모든 장바구니 메뉴가 store에 해당하는 메뉴인지 검증
		Long storeId = receipt.getSale().getStore().getStoreId();
		Set<Long> menuIds = cart.getCartMenus().stream()
			.map(cartMenu -> cartMenu.getMenuInfo().getId())
			.collect(Collectors.toSet());
		if (menuReader.countByIdIn(storeId, menuIds) != menuIds.size()) {
			throw new ServiceException(ErrorCode.MENU_NOT_FOUND);
		}

		// 품절 여부 검증
		for (CartMenu cartMenu : cart.getCartMenus()) {
			if (cartMenu.getMenuInfo().isSoldOut()) {
				throw new ServiceException(ErrorCode.MENU_SOLD_OUT);
			}
		}

		// 로그인 유저의 주문일 경우, 주문자 정보 등록
		if (userPassport.getUserRole().isHigherOrEqual(UserRole.ROLE_USER)
			&& !receiptValidator.isStoreOwner(receipt, userPassport)) {
			receiptCustomerWriter.postReceiptCustomer(userPassport.getUserId(), receiptId);
		}

		Order order = orderWriter.postOrderWithCart(receipt, cart.getCartMenus());
		cartWriter.deleteCartAndCartMenuByReceiptId(receiptId);
		return order;
	}

	@Transactional
	public Order postOrderWithoutCart(Long receiptId, UserPassport userPassport, List<OrderMenu> orderMenus) {
		Receipt receipt = receiptReader.getNonStopReceiptsWithTableAndStoreAndLock(receiptId).orElseThrow(
			() -> new ServiceException(ErrorCode.RECEIPT_NOT_FOUND));

		saleValidator.validateSaleOpen(receipt.getSale());
		receiptValidator.validateIsOwner(receipt, userPassport);

		Long storeId = receipt.getSale().getStore().getStoreId();
		for (OrderMenu orderMenu : orderMenus) {
			MenuInfo menuInfo = menuReader.getMenuInfo(storeId, orderMenu.getMenu().getMenuInfo().getId())
				.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));
			orderMenu.getMenu().changeFullInfoMenuInfo(menuInfo);

			if (menuInfo.isSoldOut()) {
				throw new ServiceException(ErrorCode.MENU_SOLD_OUT);
			}
		}

		return orderWriter.postOrderWithoutCart(receipt, orderMenus);
	}

	// 주문 취소, 주문 접수만 가능
	public Order patchOrderStatus(Long orderId, UserPassport userPassport, OrderStatus orderStatus) {
		Order order = orderReader.getOrderWithStore(orderId)
			.orElseThrow(() -> new ServiceException(ErrorCode.ORDER_NOT_FOUND));

		UserRole userRole = receiptValidator.validateRole(order.getReceipt(), userPassport);

		return orderWriter.patchOrderStatus(order, orderStatus, userRole);
	}

	public List<Order> getReceiptOrders(Long receiptId) {
		receiptValidator.validateReceipt(receiptId);
		return orderReader.getReceiptOrdersWithMenu(receiptId);
	}

	public List<Order> getSaleOrders(Long saleId, List<OrderStatus> orderStatuses, UserPassport userPassport) {
		Sale sale = saleReader.readSingleSale(saleId)
			.orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND_SALE));
		if (!sale.getStore().getOwnerPassport().getUserId().equals(userPassport.getUserId())
			|| userPassport.getUserRole() != UserRole.ROLE_OWNER) {
			throw new ServiceException(ErrorCode.SALE_ACCESS_DENIED);
		}

		return orderReader.getSaleOrdersWithMenuAndTable(saleId, orderStatuses);
	}

	public Order getOrder(Long orderId) {
		return orderReader.getOrderWithMenu(orderId)
			.orElseThrow(() -> new ServiceException(ErrorCode.ORDER_NOT_FOUND));
	}
}
