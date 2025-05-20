package domain.pos.order.implement;

import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserRole;

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

	public OrderMenu patchOrderMenuStatus(OrderMenu orderMenu, OrderMenuStatus orderMenuStatus,
		UserRole requesterRole) {
		if (orderMenuStatus == OrderMenuStatus.COOKING) {
			orderMenu.getOrderMenuStatus().reCookingOrderMenu(orderMenu.getOrderMenuId(), requesterRole);
		} else if (orderMenuStatus == OrderMenuStatus.CANCELED) {
			orderMenu.getOrderMenuStatus().cancelOrderMenu(orderMenu.getOrderMenuId(), requesterRole);
		} else if (orderMenuStatus == OrderMenuStatus.COMPLETED) {
			orderMenu.getOrderMenuStatus().completeOrderMenu(orderMenu.getOrderMenuId(), requesterRole);
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

	public OrderMenu patchOrderMenuQuantity(OrderMenu orderMenu, Integer patchQuantity) {
		return orderMenuRepository.patchOrderMenuQuantity(orderMenu, patchQuantity);
	}

	public OrderMenu patchOrderMenuCompletedCount(OrderMenu orderMenu, Integer patchCompletedCount) {
		return orderMenuRepository.patchOrderMenuCompletedCount(orderMenu, patchCompletedCount);
	}
}
