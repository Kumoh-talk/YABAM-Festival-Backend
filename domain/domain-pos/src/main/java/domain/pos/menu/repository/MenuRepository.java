package domain.pos.menu.repository;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import domain.pos.member.entity.UserPassport;
import domain.pos.menu.entity.Menu;
import domain.pos.menu.entity.MenuInfo;

public interface MenuRepository {
	Menu postMenu(Long storeId, UserPassport userPassport, Long menuCategoryId, MenuInfo menuInfo);

	Optional<MenuInfo> getMenuInfo(Long menuId);

	Slice<MenuInfo> getMenuSlice(Pageable pageable, MenuInfo lastMenuInfo, Long menuCategoryId);

	boolean existsMenuOrder(Long menuCategoryId, int menuOrder);

	// TODO : patch 할 때, order, MenuCategory는 수정하지 않도록 구현
	MenuInfo patchMenu(MenuInfo patchMenuInfo);

	// TODO : Infra 계층 구현 시 동일 가게, 동일 카테고리 내에 지정 order 이상인 메뉴들은 ++order 하도록 구현
	MenuInfo patchMenuOrder(Long storeId, Long menuCategoryId, Long menuId, int patchOrder);

	// TODO : Infra 계층 구현 시 동일 가게, 동일 카테고리 내에 삭제 메뉴 order 초과인 메뉴들은 --order 하도록 구현
	void deleteMenu(Long storeId, Long menuCategoryId, Long menuId);

}
