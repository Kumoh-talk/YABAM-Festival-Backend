package domain.pos.menu.implement;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import domain.pos.menu.entity.Menu;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MenuReader {
	private final MenuRepository menuRepository;

	public Optional<MenuInfo> getMenuInfo(Long storeId, Long menuId) {
		return menuRepository.getMenuInfo(storeId, menuId);
	}

	public Optional<Menu> getMenuWithCategoryAndStoreLock(Long storeId, Long menuId) {
		List<Menu> menus = menuRepository.getAllByStoreIdWithCategoryAndLock(storeId);
		return menus.stream()
			.filter(menu -> menu.getMenuInfo().getId().equals(menuId))
			.findFirst();
	}

	public Slice<MenuInfo> getMenuSlice(int pageSize, MenuInfo lastMenuInfo, Long menuCategoryId) {
		return menuRepository.getMenuSlice(pageSize, lastMenuInfo, menuCategoryId);
	}

	public boolean existsMenu(Long storeId, Long menuId) {
		return menuRepository.existsMenu(storeId, menuId);
	}

	public boolean existsMenuOrder(Long menuCategoryId, int menuOrder) {
		return menuRepository.existsMenuOrder(menuCategoryId, menuOrder);
	}

	public Long countByIdIn(Long storeId, Set<Long> menuIds) {
		return menuRepository.countByIdIn(storeId, menuIds);
	}

}
