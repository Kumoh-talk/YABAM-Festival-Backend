package domain.pos.menu.implement;

import org.springframework.stereotype.Component;

import domain.pos.menu.entity.Menu;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.menu.repository.MenuRepository;
import domain.pos.store.entity.Store;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MenuWriter {
	private final MenuRepository menuRepository;

	public Menu postMenu(Store store, MenuCategoryInfo menuCategory, MenuInfo menuInfo) {
		return menuRepository.postMenu(store, menuCategory, menuInfo);
	}

	public MenuInfo patchMenu(MenuInfo patchMenuInfo) {
		return menuRepository.patchMenu(patchMenuInfo);
	}

	public MenuInfo patchMenuOrder(Menu menu, Integer patchOrder) {
		return menuRepository.patchMenuOrder(menu, patchOrder);
	}

	public void deleteMenu(Menu menu) {
		menuRepository.deleteMenu(menu);
	}

}
