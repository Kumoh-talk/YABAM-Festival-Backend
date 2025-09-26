package domain.pos.menu.adapter.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import domain.pos.menu.entity.v2.domain.Menu;
import domain.pos.menu.entity.v2.domain.state.MenuInfoState;
import domain.pos.menu.implement.MenuCategoryValidator;
import domain.pos.menu.implement.v2.OrderAllocator;
import domain.pos.menu.implement.v2.OrderingOps;
import domain.pos.menu.port.provided.MenuCommand;
import domain.pos.menu.port.required.MenuRepository;
import domain.pos.store.entity.Store;
import domain.pos.store.implement.StoreValidator;

@Service
public class MenuCommandImpl implements MenuCommand {
	private final StoreValidator storeValidator;
	private final MenuCategoryValidator menuCategoryValidator;

	private final OrderAllocator<Menu> menuOrderAllocator;

	private final MenuRepository menuRepository;

	public MenuCommandImpl(
		StoreValidator storeValidator,
		MenuCategoryValidator menuCategoryValidator,
		@Qualifier("menuOrderingOps") OrderingOps<Menu> menuOrderingOps,
		MenuRepository menuRepository
	) {
		this.storeValidator = storeValidator;
		this.menuCategoryValidator = menuCategoryValidator;
		this.menuOrderAllocator = new OrderAllocator<>(menuOrderingOps);
		this.menuRepository = menuRepository;
	}

	@Transactional
	@Override
	public Menu create(UserPassport userPassport, Long storeId, Long menuCategoryId,
		MenuInfoState createMenuInfoState) {
		Store store = storeValidator.validateStoreOwner(userPassport, storeId);
		validateStoreOpen(store);
		menuCategoryValidator.validateMenuCategory(storeId, menuCategoryId);

		Integer newMenuOrder = menuOrderAllocator.allocateNewOrder(menuCategoryId);
		Menu newMenu = Menu.create(createMenuInfoState, newMenuOrder, storeId, menuCategoryId);

		return menuRepository.create(newMenu);
	}

	@Transactional
	@Override
	public Menu updateMenuInfo(UserPassport userPassport, Long storeId, Long menuId,
		MenuInfoState updateMenuInfoState) {
		Store store = storeValidator.validateStoreOwner(userPassport, storeId);
		validateStoreOpen(store);
		Menu currentMenu = menuRepository.readMenu(storeId, menuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));

		boolean isChanged = currentMenu.updateMenuInfo(updateMenuInfoState);
		return isChanged ? menuRepository.updateMenuInfo(currentMenu) : currentMenu;
	}

	@Transactional
	@Override
	public Menu updateMenuOrder(UserPassport userPassport, Long storeId,
		Long menuId, Integer updateOrder) {
		Store store = storeValidator.validateStoreOwner(userPassport, storeId);
		validateStoreOpen(store);
		Menu menu = menuRepository.readMenu(storeId, menuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));
		Integer previousOrder = menu.getOrder();

		if (menu.updateOrder(updateOrder)) {
			return menuOrderAllocator.relocationOrders(userPassport.getUserId(), storeId,
					menu.getMenuCategoryId(), menu.getId(), menu.getOrder(), previousOrder)
				.orElseThrow(() -> new ServiceException(ErrorCode.INVALID_INPUT_VALUE));
		}

		return menu;
	}

	@Transactional
	@Override
	public Menu updateIsSoldOut(UserPassport userPassport, Long storeId,
		Long menuId, Boolean updateIsSoldOut) {
		storeValidator.validateStoreOwner(userPassport, storeId);
		Menu menu = menuRepository.readMenu(storeId, menuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));

		boolean isChanged = menu.updateIsSoldOut(updateIsSoldOut);
		return isChanged ? menuRepository.updateState(menu) : menu;
	}

	@Transactional
	@Override
	public Menu updateIsRecommended(UserPassport userPassport, Long storeId,
		Long menuId, Boolean updateIsRecommended) {
		storeValidator.validateStoreOwner(userPassport, storeId);
		Menu menu = menuRepository.readMenu(storeId, menuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));

		boolean isChanged = menu.updateIsRecommended(updateIsRecommended);
		return isChanged ? menuRepository.updateState(menu) : menu;
	}

	@Transactional
	@Override
	public void delete(UserPassport userPassport, Long storeId,
		Long menuId) {
		Store store = storeValidator.validateStoreOwner(userPassport, storeId);
		validateStoreOpen(store);
		Menu menu = menuRepository.readMenu(storeId, menuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));

		menuOrderAllocator.deleteOrder(userPassport.getUserId(), storeId, menu.getMenuCategoryId(), menuId);
		menuRepository.delete(menuId);
	}

	private void validateStoreOpen(Store store) {
		if (store.getIsOpen()) {
			throw new ServiceException(ErrorCode.STORE_IS_OPEN_MENU_WRITE);
		}
	}
}
