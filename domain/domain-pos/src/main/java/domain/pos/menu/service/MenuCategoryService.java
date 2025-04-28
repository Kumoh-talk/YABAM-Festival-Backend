package domain.pos.menu.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserPassport;
import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.menu.implement.MenuCategoryReader;
import domain.pos.menu.implement.MenuCategoryValidator;
import domain.pos.menu.implement.MenuCategoryWriter;
import domain.pos.store.entity.Store;
import domain.pos.store.implement.StoreReader;
import domain.pos.store.implement.StoreValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuCategoryService {
	private final StoreValidator storeValidator;
	private final MenuCategoryValidator menuCategoryValidator;
	private final StoreReader storeReader;
	private final MenuCategoryWriter menuCategoryWriter;
	private final MenuCategoryReader menuCategoryReader;

	public MenuCategory postMenuCategory(Long storeId, UserPassport userPassport, MenuCategoryInfo menuCategoryInfo) {
		Store store = storeValidator.validateStoreOwner(userPassport, storeId);
		validateStoreOpen(store);

		menuCategoryValidator.validateMenuCategoryOrder(storeId, menuCategoryInfo);
		return menuCategoryWriter.postMenuCategory(store, menuCategoryInfo);
	}

	public List<MenuCategoryInfo> getMenuCategoryList(Long storeId) {
		storeReader.readSingleStore(storeId).orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND_STORE));
		return menuCategoryReader.getMenuCategoryInfoList(storeId);
	}

	@Transactional
	public MenuCategoryInfo patchMenuCategory(Long storeId, UserPassport userPassport,
		MenuCategoryInfo patchMenuCategoryInfo) {
		Store store = storeValidator.validateStoreOwner(userPassport, storeId);
		validateStoreOpen(store);

		menuCategoryReader.getMenuCategoryInfo(storeId, patchMenuCategoryInfo.getId())
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
		return menuCategoryWriter.patchMenuCategory(patchMenuCategoryInfo);
	}

	// TODO : 컨트롤러에서 patchOrder not null 및 양수체크
	@Transactional
	public MenuCategoryInfo patchMenuCategoryOrder(Long storeId, UserPassport userPassport, Long menuCategoryId,
		Integer patchOrder) {
		Store store = storeValidator.validateStoreOwner(userPassport, storeId);
		validateStoreOpen(store);

		MenuCategoryInfo menuCategoryInfo = menuCategoryReader.getMenuCategoryInfoWithStoreLock(storeId, menuCategoryId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
		return menuCategoryWriter.patchMenuCategoryOrder(storeId, menuCategoryInfo, patchOrder);
	}

	public void deleteMenuCategory(Long storeId, UserPassport userPassport, Long categoryId) {
		Store store = storeValidator.validateStoreOwner(userPassport, storeId);
		validateStoreOpen(store);

		menuCategoryReader.getMenuCategoryInfoWithStoreLock(storeId, categoryId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
		menuCategoryWriter.deleteMenuCategory(storeId, categoryId);
	}

	private void validateStoreOpen(Store store) {
		if (store.getIsOpen()) {
			log.warn("가게가 운영중입니다. 카테고리 생성 및 수정 불가 : storeId={}", store.getStoreId());
			throw new ServiceException(ErrorCode.STORE_IS_OPEN_MENU_WRITE);
		}
	}
}
