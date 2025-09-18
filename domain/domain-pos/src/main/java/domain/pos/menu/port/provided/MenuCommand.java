package domain.pos.menu.port.provided;

import com.vo.UserPassport;

import domain.pos.menu.entity.v2.domain.Menu;
import domain.pos.menu.entity.v2.domain.state.MenuInfoState;

public interface MenuCommand {
	Menu create(UserPassport userPassport, Long storeId,
		Long menuCategoryId, MenuInfoState createMenuInfoState);

	Menu updateMenuInfo(UserPassport userPassport, Long storeId,
		Long menuId, MenuInfoState updateMenuInfoState);

	Menu updateMenuOrder(UserPassport userPassport, Long storeId,
		Long menuId, Integer updateOrder);

	Menu updateIsSoldOut(UserPassport userPassport, Long storeId,
		Long menuId, Boolean updateIsSoldOut);

	Menu updateIsRecommended(UserPassport userPassport, Long storeId,
		Long menuId, Boolean updateIsRecommended);

	void delete(UserPassport userPassport, Long storeId,
		Long menuId);

}
