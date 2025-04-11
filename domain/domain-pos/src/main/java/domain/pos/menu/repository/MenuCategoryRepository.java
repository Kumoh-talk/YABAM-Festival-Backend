package domain.pos.menu.repository;

import java.util.List;
import java.util.Optional;

import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuCategoryInfo;

public interface MenuCategoryRepository {
	MenuCategory postMenuCategory(Long storeId, MenuCategoryInfo menuCategoryInfo);

	Optional<MenuCategory> getMenuCategory(Long storeId, Long categoryId);

	List<MenuCategory> getMenuCategoryList(Long storeId);

	boolean existsMenuCategory(Long storeId, Long menuCategoryId);

	boolean existsMenuCategoryOrder(Long storeId, int menuCategoryOrder);

	// TODO : Native Update(카테고리 명) + patch 할 때, order는 수정하지 않도록 구현
	MenuCategoryInfo patchMenuCategory(MenuCategoryInfo menuCategoryInfo);

	// TODO : Native Update(순서) Infra 계층 구현 시 동일 가게 내에 지정 order 이상인 메뉴들은 ++order 하도록 구현
	MenuCategoryInfo patchMenuCategoryOrder(Long storeId, MenuCategoryInfo menuCategoryInfo);

	// TODO : Infra 계층 구현 시 동일 가게 내에 삭제 메뉴 카테고리 order 초과인 메뉴들은 --order 하도록 구현
	void deleteMenuCategory(Long categoryId);

}
