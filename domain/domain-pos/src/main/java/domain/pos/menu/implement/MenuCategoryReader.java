package domain.pos.menu.implement;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.repository.MenuCategoryRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MenuCategoryReader {
	private final MenuCategoryRepository menuCategoryRepository;

	public Optional<MenuCategory> getMenuCategory(Long categoryId) {
		return menuCategoryRepository.getMenuCategory(categoryId);
	}

	public List<MenuCategory> getMenuCategoryList(Long storeId) {
		return menuCategoryRepository.getMenuCategoryList(storeId);
	}

	public boolean existsMenuCategoryOrder(Long storeId, int menuCategoryOrder) {
		return menuCategoryRepository.existsMenuCategoryOrder(storeId, menuCategoryOrder);
	}
}
