package domain.pos.menu.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserPassport;
import domain.pos.menu.entity.Menu;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.menu.implement.MenuCategoryReader;
import domain.pos.menu.implement.MenuCategoryValidator;
import domain.pos.menu.implement.MenuReader;
import domain.pos.menu.implement.MenuValidator;
import domain.pos.menu.implement.MenuWriter;
import domain.pos.store.entity.Store;
import domain.pos.store.implement.StoreReader;
import domain.pos.store.implement.StoreValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {
	private final StoreValidator storeValidator;
	private final MenuValidator menuValidator;
	private final MenuCategoryValidator menuCategoryValidator;

	private final StoreReader storeReader;
	private final MenuCategoryReader menuCategoryReader;
	private final MenuReader menuReader;
	private final MenuWriter menuWriter;

	public Menu postMenu(Long storeId, UserPassport userPassport, Long menuCategoryId, MenuInfo menuInfo) {
		Store store = storeValidator.validateStoreOwner(userPassport, storeId);
		validateStoreOpen(store);

		MenuCategoryInfo menuCategoryInfo = menuCategoryReader.getMenuCategoryInfo(storeId, menuCategoryId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
		menuValidator.validateMenuOrder(menuCategoryId, menuInfo);
		return menuWriter.postMenu(store, menuCategoryInfo, menuInfo);
	}

	public MenuInfo getMenuInfo(Long storeId, Long menuId) {
		storeReader.readSingleStore(storeId)
			.orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND_STORE));
		return menuReader.getMenuInfo(storeId, menuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));
	}

	public Slice<MenuInfo> getMenuSlice(Pageable pageable, Long lastMenuId, Long storeId, Long menuCategoryId) {
		storeReader.readSingleStore(storeId)
			.orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND_STORE));
		menuCategoryValidator.validateMenuCategory(storeId, menuCategoryId);

		MenuInfo lastMenuInfo = null;
		if (lastMenuId != null) {
			lastMenuInfo = menuReader.getMenuInfo(storeId, lastMenuId)
				.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));
		}
		return menuReader.getMenuSlice(pageable, lastMenuInfo, menuCategoryId);
	}

	@Transactional
	public MenuInfo patchMenu(Long storeId, UserPassport userPassport, MenuInfo patchMenuInfo) {
		Store store = storeValidator.validateStoreOwner(userPassport, storeId);
		validateStoreOpen(store);

		menuReader.getMenuInfo(storeId, patchMenuInfo.getId())
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));
		return menuWriter.patchMenu(patchMenuInfo);
	}

	@Transactional
	public MenuInfo patchMenuOrder(Long storeId, UserPassport userPassport, Long menuId,
		Integer patchOrder) {
		Store store = storeValidator.validateStoreOwner(userPassport, storeId);
		validateStoreOpen(store);

		Menu menu = menuReader.getMenuWithCategoryAndStoreLock(storeId, menuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));
		return menuWriter.patchMenuOrder(menu, patchOrder);
	}

	@Transactional
	public void deleteMenu(Long storeId, UserPassport userPassport, Long menuId) {
		Store store = storeValidator.validateStoreOwner(userPassport, storeId);
		validateStoreOpen(store);

		Menu menu = menuReader.getMenuWithCategoryAndStoreLock(storeId, menuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));
		menuWriter.deleteMenu(menu);
	}

	private void validateStoreOpen(Store store) {
		if (store.getIsOpen()) {
			log.warn("가게가 운영중입니다. 메뉴 생성 및 수정 불가 : storeId={}", store.getStoreId());
			throw new ServiceException(ErrorCode.STORE_IS_OPEN_MENU_WRITE);
		}
	}
}
