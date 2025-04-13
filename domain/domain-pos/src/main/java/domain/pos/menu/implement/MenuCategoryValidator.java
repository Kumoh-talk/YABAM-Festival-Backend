package domain.pos.menu.implement;

import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.menu.entity.MenuCategoryInfo;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MenuCategoryValidator {
	private final MenuCategoryReader menuCategoryReader;

	public void validateMenuCategory(Long categoryId) {
		menuCategoryReader.getMenuCategory(categoryId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
	}

	public void validateMenuCategoryOrder(Long storeId, MenuCategoryInfo menuCategoryInfo) {
		if (menuCategoryReader.existsMenuCategoryOrder(storeId, menuCategoryInfo.getMenuCategoryOrder())) {
			throw new ServiceException(ErrorCode.EXIST_MENU_CATEGORY_ORDER);
		}
	}
}
