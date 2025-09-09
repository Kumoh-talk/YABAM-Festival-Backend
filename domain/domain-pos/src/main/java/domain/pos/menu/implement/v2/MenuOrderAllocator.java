package domain.pos.menu.implement.v2;

import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.menu.entity.v2.Menu;
import domain.pos.menu.port.required.MenuCategoryRepository;
import domain.pos.menu.port.required.MenuRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MenuOrderAllocator {
	private final MenuRepository menuRepository;
	private final MenuCategoryRepository menuCategoryRepository;

	public static final Integer TEMPORARY_ORDER = 0;

	public Integer allocateNewMenuOrder(Long menuCategoryId) {
		menuCategoryRepository.lockMenuCategory(menuCategoryId);
		return menuRepository.readMaxMenuOrder(menuCategoryId) + 1;
	}

	public Menu relocationOrders(Menu updatedMenu, Integer previousOrder) {
		Long menuCategoryId = updatedMenu.getMenuCategoryId();
		Integer updatedOrder = updatedMenu.getOrder();

		menuCategoryRepository.lockMenuCategory(menuCategoryId);
		Integer maxOrder = menuRepository.readMaxMenuOrder(menuCategoryId);
		if (updatedOrder > maxOrder) {
			throw new ServiceException(ErrorCode.DOMAIN_INVALID_MENU_ORDER);
		}

		menuRepository.updateTemporaryOrder(updatedMenu.getId(), TEMPORARY_ORDER);
		// TODO : dirty checking이면 update 순서 신경써야함

		if (previousOrder < updatedOrder) {
			menuRepository.decrementMenuOrdersInRange(menuCategoryId, previousOrder + 1, updatedOrder);
		} else {
			menuRepository.incrementMenuOrdersInRange(menuCategoryId, updatedOrder, previousOrder - 1);
		}

		return menuRepository.updateOrder(updatedMenu.getId(), updatedOrder);
	}

	public void delete(Menu menu) {
		Long menuCategoryId = menu.getMenuCategoryId();
		Integer currentOrder = menu.getOrder();

		menuCategoryRepository.lockMenuCategory(menuCategoryId);

		menuRepository.updateOrder(menu.getId(), null);
		menuRepository.deleteMenu(menu.getId());
		menuRepository.decrementMenuOrdersInRange(menuCategoryId, currentOrder + 1, Integer.MAX_VALUE);
	}
}
