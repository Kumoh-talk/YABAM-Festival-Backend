package domain.pos.menu.adapter.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import domain.pos.menu.entity.v2.domain.MenuCategory;
import domain.pos.menu.implement.v2.OrderAllocator;
import domain.pos.menu.implement.v2.OrderingOps;
import domain.pos.menu.port.provided.MenuCategoryCommand;
import domain.pos.menu.port.required.MenuCategoryRepository;
import domain.pos.store.entity.Store;
import domain.pos.store.implement.StoreValidator;

@Service
public class MenuCategoryCommandImpl implements MenuCategoryCommand {

	private final StoreValidator storeValidator;

	private final OrderAllocator<MenuCategory> menuCategoryOrderAllocator;

	private final MenuCategoryRepository menuCategoryRepository;

	public MenuCategoryCommandImpl(
		StoreValidator storeValidator,
		@Qualifier("menuCategoryOrderingOps") OrderingOps<MenuCategory> menuCategoryOrderingOps,
		MenuCategoryRepository menuCategoryRepository) {
		this.storeValidator = storeValidator;
		this.menuCategoryOrderAllocator = new OrderAllocator<>(menuCategoryOrderingOps);
		this.menuCategoryRepository = menuCategoryRepository;
	}

	@Transactional
	@Override
	public MenuCategory create(UserPassport userPassport, Long storeId, String menuCategoryName) {
		Store store = storeValidator.validateStoreOwner(userPassport, storeId);
		validateStoreOpen(store);

		Integer newMenuCategoryOrder = menuCategoryOrderAllocator.allocateNewOrder(storeId);
		MenuCategory menuCategory = MenuCategory.create(menuCategoryName, newMenuCategoryOrder, storeId);

		return menuCategoryRepository.create(userPassport.getUserId(), storeId, menuCategory)
			.orElseThrow(() -> new ServiceException(ErrorCode.INVALID_INPUT_VALUE));
	}

	@Transactional
	@Override
	public MenuCategory updateName(UserPassport userPassport, Long storeId, Long menuCategoryId, String updateName) {
		Store store = storeValidator.validateStoreOwner(userPassport, storeId);
		validateStoreOpen(store);

		MenuCategory menuCategory = menuCategoryRepository.readMenuCategory(storeId, menuCategoryId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
		boolean isChanged = menuCategory.updateName(updateName);
		if (!isChanged) {
			return menuCategory;
		}

		return menuCategoryRepository.updateName(userPassport.getUserId(), storeId, menuCategory)
			.orElseThrow(() -> new ServiceException(ErrorCode.INVALID_INPUT_VALUE));
	}

	@Transactional
	@Override
	public MenuCategory updateOrder(UserPassport userPassport, Long storeId, Long menuCategoryId, Integer updateOrder) {
		Store store = storeValidator.validateStoreOwner(userPassport, storeId);
		validateStoreOpen(store);

		MenuCategory menuCategory = menuCategoryRepository.readMenuCategory(storeId, menuCategoryId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
		Integer previousOrder = menuCategory.getOrder();

		if (menuCategory.updateOrder(updateOrder)) {
			return menuCategoryOrderAllocator.relocationOrders(userPassport.getUserId(), storeId, storeId,
					menuCategoryId, menuCategory.getOrder(), previousOrder)
				.orElseThrow(() -> new ServiceException(ErrorCode.INVALID_INPUT_VALUE));
		}
		return menuCategory;
	}

	@Transactional
	@Override
	public void delete(UserPassport userPassport, Long storeId, Long menuCategoryId) {
		validateForDelete(userPassport, storeId, menuCategoryId);

		menuCategoryOrderAllocator.deleteOrder(userPassport.getUserId(), storeId, storeId, menuCategoryId);
		menuCategoryRepository.delete(userPassport.getUserId(), storeId, menuCategoryId)
			.orElseThrow(() -> new ServiceException(ErrorCode.INVALID_INPUT_VALUE));
	}

	private void validateStoreOpen(Store store) {
		if (store.getIsOpen()) {
			throw new ServiceException(ErrorCode.STORE_IS_OPEN_MENU_CATEGORY_WRITE);
		}
	}

	private void validateForDelete(UserPassport userPassport, Long storeId, Long menuCategoryId) {
		Store store = storeValidator.validateStoreOwner(userPassport, storeId);
		validateStoreOpen(store);

		menuCategoryRepository.readMenuCategory(storeId, menuCategoryId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
	}
}
