package domain.pos.menu.port.provided;

import com.vo.UserPassport;

import domain.pos.menu.entity.v2.domain.MenuCategory;

public interface MenuCategoryCommand {
	MenuCategory create(UserPassport userPassport, Long storeId, String menuCategoryName);

	MenuCategory updateName(UserPassport userPassport, Long storeId, Long menuCategoryId, String updateName);

	MenuCategory updateOrder(UserPassport userPassport, Long storeId, Long menuCategoryId,
		Integer updateOrder);

	void delete(UserPassport userPassport, Long storeId, Long categoryId);
}
