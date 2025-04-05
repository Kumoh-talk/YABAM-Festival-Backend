package domain.pos.menu.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.implement.MenuCategoryReader;
import domain.pos.menu.implement.MenuCategoryValidator;
import domain.pos.menu.implement.MenuCategoryWriter;
import domain.pos.store.implement.StoreReader;
import domain.pos.store.implement.StoreValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuCategoryService {
	private final StoreValidator storeValidator;
	private final MenuCategoryValidator menuCategoryValidator;
	private final StoreReader storeReader;
	private final MenuCategoryWriter menuCategoryWriter;
	private final MenuCategoryReader menuCategoryReader;

	public MenuCategory postMenuCategory(Long storeId, Long userId, String categoryName) {
		storeValidator.validateStoreOwner(storeId, userId);
		return menuCategoryWriter.postMenuCategory(storeId, categoryName);
	}

	public List<MenuCategory> getMenuCategoryList(Long storeId) {
		storeReader.readSingleStore(storeId).orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND_STORE));
		return menuCategoryReader.getMenuCategoryList(storeId);
	}

	public MenuCategory patchMenuCategory(Long storeId, Long userId, Long categoryId, String categoryName) {
		storeValidator.validateStoreOwner(storeId, userId);
		menuCategoryValidator.validateMenuCategory(categoryId);
		return menuCategoryWriter.patchMenuCategory(categoryId, categoryName);
	}

	public void deleteMenuCategory(Long storeId, Long userId, Long categoryId) {
		storeValidator.validateStoreOwner(storeId, userId);
		menuCategoryValidator.validateMenuCategory(categoryId);
		menuCategoryWriter.deleteMenuCategory(categoryId);
	}
}
