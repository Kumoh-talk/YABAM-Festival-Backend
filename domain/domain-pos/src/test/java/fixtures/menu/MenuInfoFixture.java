package fixtures.menu;

import domain.pos.menu.entity.MenuInfo;

public class MenuInfoFixture {
	public static final Long GENERAL_MENU_ID = 1L;
	public static final int GENERAL_MENU_ORDER = 1;
	public static final String GENERAL_MENU_NAME = "menu name";
	public static final int GENERAL_PRICE = 10000;
	public static final String GENERAL_DESCRIPTION = "menu description";
	public static final String GENERAL_IMAGE_URL = "imageURL";
	public static final boolean GENERAL_IS_SOLD_OUT = false;
	public static final boolean GENERAL_IS_RECOMMENDED = false;

	public static MenuInfo REQUEST_MENU_INFO() {
		return MenuInfo.builder()
			.menuName(GENERAL_MENU_NAME)
			.menuOrder(GENERAL_MENU_ORDER)
			.price(GENERAL_PRICE)
			.description(GENERAL_DESCRIPTION)
			.imageUrl(GENERAL_IMAGE_URL)
			.isSoldOut(GENERAL_IS_SOLD_OUT)
			.isRecommended(GENERAL_IS_RECOMMENDED)
			.build();
	}

	public static MenuInfo GENERAL_MENU_INFO() {
		return MenuInfo.builder()
			.menuId(GENERAL_MENU_ID)
			.menuOrder(GENERAL_MENU_ORDER)
			.menuName(GENERAL_MENU_NAME)
			.price(GENERAL_PRICE)
			.description(GENERAL_DESCRIPTION)
			.imageUrl(GENERAL_IMAGE_URL)
			.isSoldOut(GENERAL_IS_SOLD_OUT)
			.isRecommended(GENERAL_IS_RECOMMENDED)
			.build();
	}

	public static MenuInfo CUSTOM_MENU_INFO(Long menuId, int menuOrder, String menuName, int price, String description,
		String imageUrl, boolean isSoldOut, boolean isRecommended) {
		return MenuInfo.builder()
			.menuId(menuId)
			.menuOrder(menuOrder)
			.menuName(menuName)
			.price(price)
			.description(description)
			.imageUrl(imageUrl)
			.isSoldOut(isSoldOut)
			.isRecommended(isRecommended)
			.build();
	}

	public static MenuInfo REQUEST_TO_ENTITY(Long menuId, MenuInfo requestMenuInfo) {
		return MenuInfo.builder()
			.menuId(menuId)
			.menuOrder(requestMenuInfo.getMenuOrder())
			.menuName(requestMenuInfo.getMenuName())
			.price(requestMenuInfo.getPrice())
			.description(requestMenuInfo.getDescription())
			.imageUrl(requestMenuInfo.getImageUrl())
			.isSoldOut(requestMenuInfo.isSoldOut())
			.isRecommended(requestMenuInfo.isRecommended())
			.build();
	}
}
