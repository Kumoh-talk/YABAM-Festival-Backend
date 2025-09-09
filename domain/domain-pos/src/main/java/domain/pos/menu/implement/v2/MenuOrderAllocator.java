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

	public Menu relocationOrders(Menu menu, Integer updateOrder) {
		Long menuCategoryId = menu.getMenuCategoryId();
		Integer maxOrder = menuRepository.readMaxMenuOrder(menuCategoryId);
		if (updateOrder > maxOrder) {
			throw new ServiceException(ErrorCode.DOMAIN_INVALID_MENU_ORDER);
		}

		menuCategoryRepository.lockMenuCategory(menuCategoryId);
		menuRepository.updateTemporaryOrder(menu.getId(), TEMPORARY_ORDER);
		// TODO : dirty checking이면 update 순서 신경써야함

		Integer currentOrder = menu.getOrder();
		if (currentOrder < updateOrder) {
			menuRepository.decrementMenuOrdersInRange(menuCategoryId, currentOrder + 1, updateOrder);
		} else {
			menuRepository.incrementMenuOrdersInRange(menuCategoryId, updateOrder, currentOrder - 1);
		}

		return menuRepository.updateOrder(menu.getId(), updateOrder);
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
