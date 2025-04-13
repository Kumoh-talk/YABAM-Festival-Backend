package domain.pos.menu.implement;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import domain.pos.menu.entity.MenuInfo;
import domain.pos.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MenuReader {
	private final MenuRepository menuRepository;

	public Optional<MenuInfo> getMenuInfo(Long menuId) {
		return menuRepository.getMenuInfo(menuId);
	}

	public Slice<MenuInfo> getMenuSlice(Pageable pageable, MenuInfo lastMenuInfo, Long menuCategoryId) {
		return menuRepository.getMenuSlice(pageable, lastMenuInfo, menuCategoryId);
	}

	public boolean existsMenuOrder(Long menuCategoryId, int menuOrder) {
		return menuRepository.existsMenuOrder(menuCategoryId, menuOrder);
	}
}
