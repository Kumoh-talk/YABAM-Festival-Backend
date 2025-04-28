package domain.pos.menu.implement;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.menu.repository.MenuCategoryRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MenuCategoryReader {
	private final MenuCategoryRepository menuCategoryRepository;

	public Optional<MenuCategoryInfo> getMenuCategoryInfo(Long storeId, Long categoryId) {
		return menuCategoryRepository.getMenuCategoryInfo(storeId, categoryId);
	}

	public Optional<MenuCategoryInfo> getMenuCategoryInfoWithStoreLock(Long storeId, Long categoryId) {
		List<MenuCategoryInfo> menuCategoryInfos = menuCategoryRepository.getAllByStoreIdWithLock(storeId);
		return menuCategoryInfos.stream()
			.filter(menuCategoryInfo -> menuCategoryInfo.getId().equals(categoryId))
			.findFirst();
	}

	public List<MenuCategoryInfo> getMenuCategoryInfoList(Long storeId) {
		return menuCategoryRepository.getMenuCategoryInfoList(storeId);
	}

	public boolean existsMenuCategory(Long storeId, Long menuCategoryId) {
		return menuCategoryRepository.existsMenuCategory(storeId, menuCategoryId);
	}

	public boolean existsMenuCategoryOrder(Long storeId, int menuCategoryOrder) {
		return menuCategoryRepository.existsMenuCategoryOrder(storeId, menuCategoryOrder);
	}
}
