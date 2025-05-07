package domain.pos.order.implement;

import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.menu.entity.MenuInfo;
import domain.pos.order.entity.Order;
import domain.pos.order.entity.OrderMenu;
import domain.pos.order.entity.vo.OrderMenuStatus;
import domain.pos.order.repository.OrderMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderMenuWriter {
	private final OrderMenuRepository orderMenuRepository;

	public OrderMenu patchOrderMenuStatus(OrderMenu orderMenu, OrderMenuStatus orderMenuStatus) {
		if (orderMenuStatus == OrderMenuStatus.COOKING) {
			orderMenu.getOrderMenuStatus().reCookingOrderMenu(orderMenu.getOrderMenuId());
		} else if (orderMenuStatus == OrderMenuStatus.CANCELED) {
			orderMenu.getOrderMenuStatus().cancelOrderMenu(orderMenu.getOrderMenuId());
		} else if (orderMenuStatus == OrderMenuStatus.COMPLETED) {
			orderMenu.getOrderMenuStatus().completeOrderMenu(orderMenu.getOrderMenuId());
		} else {
			log.warn("올바른 변경 요청이 아닙니다 : orderMenuStatus={}", orderMenuStatus);
			throw new ServiceException(ErrorCode.INVALID_STATE_TRANSITION);
		}

		return orderMenuRepository.patchOrderMenuStatus(orderMenu, orderMenuStatus);
	}

	public OrderMenu postOrderMenu(MenuInfo menuInfo, Integer quantity, Order order) {
		return orderMenuRepository.postOrderMenu(menuInfo, quantity, order);
	}

	public void deleteOrderMenu(OrderMenu orderMenu) {
		orderMenuRepository.deleteOrderMenu(orderMenu);
	}

}
