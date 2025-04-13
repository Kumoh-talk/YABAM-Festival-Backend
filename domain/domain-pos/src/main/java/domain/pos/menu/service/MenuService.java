package domain.pos.menu.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserPassport;
import domain.pos.menu.entity.Menu;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.menu.implement.MenuCategoryValidator;
import domain.pos.menu.implement.MenuReader;
import domain.pos.menu.implement.MenuValidator;
import domain.pos.menu.implement.MenuWriter;
import domain.pos.store.implement.StoreReader;
import domain.pos.store.implement.StoreValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuService {
	private final StoreValidator storeValidator;
	private final MenuValidator menuValidator;
	private final MenuCategoryValidator menuCategoryValidator;

	private final StoreReader storeReader;
	private final MenuReader menuReader;
	private final MenuWriter menuWriter;

	public Menu postMenu(Long storeId, UserPassport userPassport, Long menuCategoryId, MenuInfo menuInfo) {
		storeValidator.validateStoreOwner(userPassport, storeId);
		menuCategoryValidator.validateMenuCategory(menuCategoryId);
		menuValidator.validateMenuOrder(menuCategoryId, menuInfo);
		return menuWriter.postMenu(storeId, userPassport, menuCategoryId, menuInfo);
	}

	public MenuInfo getMenuInfo(Long storeId, Long menuId) {
		storeReader.readSingleStore(storeId)
			.orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND_STORE));
		return menuReader.getMenuInfo(menuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));
	}

	public Slice<MenuInfo> getMenuSlice(Pageable pageable, Long lastMenuId, Long storeId, Long menuCategoryId) {
		storeReader.readSingleStore(storeId)
			.orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND_STORE));
		menuCategoryValidator.validateMenuCategory(menuCategoryId);

		MenuInfo lastMenuInfo = null;
		if (lastMenuId != null) {
			lastMenuInfo = menuReader.getMenuInfo(lastMenuId)
				.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));
		}
		return menuReader.getMenuSlice(pageable, lastMenuInfo, menuCategoryId);
	}

	public MenuInfo patchMenu(Long storeId, UserPassport userPassport, MenuInfo patchMenuInfo) {
		storeValidator.validateStoreOwner(userPassport, storeId);
		menuValidator.validateMenu(patchMenuInfo.getMenuId());
		return menuWriter.patchMenu(patchMenuInfo);
	}

	public MenuInfo patchMenuOrder(Long storeId, UserPassport userPassport, Long menuCategoryId, Long menuId,
		int patchOrder) {
		storeValidator.validateStoreOwner(userPassport, storeId);
		menuValidator.validateMenu(menuId);
		return menuWriter.patchMenuOrder(storeId, menuCategoryId, menuId, patchOrder);
	}

	public void deleteMenu(Long storeId, UserPassport userPassport, Long menuCategoryId, Long menuId) {
		storeValidator.validateStoreOwner(userPassport, storeId);
		menuValidator.validateMenu(menuId);
		menuWriter.deleteMenu(storeId, menuCategoryId, menuId);
	}
}
