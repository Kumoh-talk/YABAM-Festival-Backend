package domain.pos.menu.implement;

import org.springframework.stereotype.Component;

import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.menu.repository.MenuCategoryRepository;
import domain.pos.store.entity.Store;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MenuCategoryWriter {
	private final MenuCategoryRepository menuCategoryRepository;

	public MenuCategory postMenuCategory(Store store, MenuCategoryInfo menuCategoryInfo) {
		return menuCategoryRepository.postMenuCategory(store, menuCategoryInfo);
	}

	public MenuCategoryInfo patchMenuCategory(MenuCategoryInfo patchMenuCategoryInfo) {
		return menuCategoryRepository.patchMenuCategory(patchMenuCategoryInfo);
	}

	public MenuCategoryInfo patchMenuCategoryOrder(Long storeId, MenuCategoryInfo menuCategoryInfo,
		Integer patchOrder) {
		return menuCategoryRepository.patchMenuCategoryOrder(storeId, menuCategoryInfo, patchOrder);
	}

	public void deleteMenuCategory(Long storeId, Long categoryId) {
		menuCategoryRepository.deleteMenuCategory(storeId, categoryId);
	}

}
