package fixtures.menu;

import domain.pos.menu.entity.MenuInfo;

public class MenuInfoFixture {
	public static final Long GENERAL_MENU_ID = 1L;
	public static final Long DIFF_MENU_ID = 2L;

	public static final Integer GENERAL_MENU_ORDER = 1;
	public static final Integer DIFF_MENU_ORDER = 2;

	public static final String GENERAL_MENU_NAME = "menu name";
	public static final String DIFF_MENU_NAME = "diff menu name";

	public static final Integer GENERAL_PRICE = 10000;
	public static final Integer DIFF_PRICE = 20000;

	public static final String GENERAL_DESCRIPTION = "menu description";
	public static final String DIFF_DESCRIPTION = "diff menu description";

	public static final String GENERAL_IMAGE_URL = "imageURL";
	public static final String DIFF_IMAGE_URL = "diffImageURL";

	public static final boolean GENERAL_IS_SOLD_OUT = false;
	public static final boolean GENERAL_IS_RECOMMENDED = false;

	public static MenuInfo REQUEST_MENU_INFO() {
		return MenuInfo.builder()
			.name(GENERAL_MENU_NAME)
			.order(GENERAL_MENU_ORDER)
			.price(GENERAL_PRICE)
			.description(GENERAL_DESCRIPTION)
			.imageUrl(GENERAL_IMAGE_URL)
			.isSoldOut(GENERAL_IS_SOLD_OUT)
			.isRecommended(GENERAL_IS_RECOMMENDED)
			.build();
	}

	public static MenuInfo GENERAL_MENU_INFO() {
		return MenuInfo.builder()
			.id(GENERAL_MENU_ID)
			.order(GENERAL_MENU_ORDER)
			.name(GENERAL_MENU_NAME)
			.price(GENERAL_PRICE)
			.description(GENERAL_DESCRIPTION)
			.imageUrl(GENERAL_IMAGE_URL)
			.isSoldOut(GENERAL_IS_SOLD_OUT)
			.isRecommended(GENERAL_IS_RECOMMENDED)
			.build();
	}

	public static MenuInfo DIFF_MENU_INFO() {
		return MenuInfo.builder()
			.id(DIFF_MENU_ID)
			.order(DIFF_MENU_ORDER)
			.name(DIFF_MENU_NAME)
			.price(DIFF_PRICE)
			.description(DIFF_DESCRIPTION)
			.imageUrl(DIFF_IMAGE_URL)
			.isSoldOut(GENERAL_IS_SOLD_OUT)
			.isRecommended(GENERAL_IS_RECOMMENDED)
			.build();
	}

	public static MenuInfo PATCH_MENU_INFO(MenuInfo menuInfo) {
		return MenuInfo.builder()
			.id(menuInfo.getId())
			.order(menuInfo.getOrder())
			.name(menuInfo.getName() + " PATCH")
			.price(menuInfo.getPrice() * 2)
			.description(menuInfo.getDescription() + " PATCH")
			.imageUrl(menuInfo.getImageUrl() + " PATCH")
			.isSoldOut(!menuInfo.isSoldOut())
			.isRecommended(!menuInfo.isRecommended())
			.build();
	}

	public static MenuInfo CUSTOM_MENU_INFO(Long menuId, Integer menuOrder, String menuName, Integer price,
		String description, String imageUrl, boolean isSoldOut, boolean isRecommended) {
		return MenuInfo.builder()
			.id(menuId)
			.order(menuOrder)
			.name(menuName)
			.price(price)
			.description(description)
			.imageUrl(imageUrl)
			.isSoldOut(isSoldOut)
			.isRecommended(isRecommended)
			.build();
	}

	public static MenuInfo REQUEST_TO_ENTITY(Long menuId, MenuInfo requestMenuInfo) {
		return MenuInfo.builder()
			.id(menuId)
			.order(requestMenuInfo.getOrder())
			.name(requestMenuInfo.getName())
			.price(requestMenuInfo.getPrice())
			.description(requestMenuInfo.getDescription())
			.imageUrl(requestMenuInfo.getImageUrl())
			.isSoldOut(requestMenuInfo.isSoldOut())
			.isRecommended(requestMenuInfo.isRecommended())
			.build();
	}
}
