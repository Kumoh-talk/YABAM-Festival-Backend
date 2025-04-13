package domain.pos.menu.implement;

import org.springframework.stereotype.Component;

import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.menu.repository.MenuCategoryRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MenuCategoryWriter {
	private final MenuCategoryRepository menuCategoryRepository;

	public MenuCategory postMenuCategory(Long storeId, MenuCategoryInfo menuCategoryInfo) {
		return menuCategoryRepository.postMenuCategory(storeId, menuCategoryInfo);
	}

	public MenuCategoryInfo patchMenuCategory(MenuCategoryInfo menuCategoryInfo) {
		return menuCategoryRepository.patchMenuCategory(menuCategoryInfo);
	}

	public MenuCategoryInfo patchMenuCategoryOrder(Long storeId, MenuCategoryInfo menuCategoryInfo) {
		return menuCategoryRepository.patchMenuCategoryOrder(storeId, menuCategoryInfo);
	}

	public void deleteMenuCategory(Long categoryId) {
		menuCategoryRepository.deleteMenuCategory(categoryId);
	}

}
