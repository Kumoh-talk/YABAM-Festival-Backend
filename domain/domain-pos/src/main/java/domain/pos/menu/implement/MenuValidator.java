package domain.pos.menu.implement;

import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.menu.entity.MenuInfo;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MenuValidator {
	private final MenuReader menuReader;

	public void validateMenu(Long menuId) {
		menuReader.getMenuInfo(menuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));
	}

	public void validateMenuOrder(Long menuCategoryId, MenuInfo menuInfo) {
		if (menuReader.existsMenuOrder(menuCategoryId, menuInfo.getMenuOrder())) {
			throw new ServiceException(ErrorCode.EXIST_MENU_ORDER);
		}
	}
}
