package domain.pos.menu.entity.v2;

import static fixtures.menu.v2.ValidMenuState.*;
import static fixtures.menu.v2.state.MenuInfoStateTestImpl.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.menu.entity.v2.domain.Menu;

class MenuCreateTest {

	@Test
	@DisplayName("메뉴 생성 성공")
	void create_success() {
		// given
		var validMenuInfo = VALID_STATE();

		// when
		Menu menu = Menu.create(validMenuInfo, VALID_ORDER_1, VALID_STORE_ID_1, VALID_MENU_CATEGORY_ID_1);

		// then
		assertSoftly(softly -> {
			softly.assertThat(menu.getOrder()).isEqualTo(VALID_ORDER_1);
			softly.assertThat(menu.getStoreId()).isEqualTo(VALID_STORE_ID_1);
			softly.assertThat(menu.getStoreId()).isEqualTo(VALID_STORE_ID_1);
			softly.assertThat(menu.getMenuCategoryId()).isEqualTo(VALID_MENU_CATEGORY_ID_1);
			softly.assertThat(menu.isSoldOut()).isFalse();
			softly.assertThat(menu.isRecommended()).isFalse();

			softly.assertThat(menu.getMenuInfo().getName()).isEqualTo(validMenuInfo.getName());
			softly.assertThat(menu.getMenuInfo().getPrice()).isEqualTo(validMenuInfo.getPrice());
			softly.assertThat(menu.getMenuInfo().getDescription()).isEqualTo(validMenuInfo.getDescription());
			softly.assertThat(menu.getMenuInfo().getImageUrl()).isEqualTo(validMenuInfo.getImageUrl());
		});
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(ints = {0, -1, -100})
	@DisplayName("order가 1 미만이면 IllegalArgumentException")
	void order_must_be_positive(Integer invalidOrder) {
		// when -> then
		assertThatThrownBy(() -> Menu.create(VALID_STATE(), invalidOrder, VALID_STORE_ID_1, VALID_MENU_CATEGORY_ID_1))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("storeId가 null이면 NullPointerException")
	void storeId_null() {
		// when -> then
		assertThatThrownBy(
			() -> Menu.create(VALID_STATE(), VALID_ORDER_1, null, VALID_MENU_CATEGORY_ID_1))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("menuCategoryId가 null이면 NullPointerException")
	void categoryId_null() {
		// when -> then
		assertThatThrownBy(
			() -> Menu.create(VALID_STATE(), VALID_ORDER_1, VALID_STORE_ID_1, null))
			.isInstanceOf(NullPointerException.class);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "   ", "\t", "\n"})
	@DisplayName("이름이 Null 또는 공백이면 ServiceException(DOMAIN_INVALID_MENU_NAME)")
	void name_null_or_blank(String menuName) {
		// given
		var menuInfoState = custom(s -> s.customName(menuName));

		// when -> then
		assertThatThrownBy(
			() -> Menu.create(menuInfoState, VALID_ORDER_1, VALID_STORE_ID_1, VALID_MENU_CATEGORY_ID_1))
			.isInstanceOf(ServiceException.class)
			.extracting(ex -> ((ServiceException)ex).getErrorCode())
			.isEqualTo(ErrorCode.DOMAIN_INVALID_MENU_NAME);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(ints = {-100, -1})
	@DisplayName("가격이 음수면 ServiceException(DOMAIN_INVALID_MENU_PRICE)")
	void price_negative(Integer price) {
		// given
		var menuInfoState = custom(s -> s.customPrice(price));

		// when -> then
		assertThatThrownBy(
			() -> Menu.create(menuInfoState, VALID_ORDER_1, VALID_STORE_ID_1, VALID_MENU_CATEGORY_ID_1))
			.isInstanceOf(ServiceException.class)
			.extracting(ex -> ((ServiceException)ex).getErrorCode())
			.isEqualTo(ErrorCode.DOMAIN_INVALID_MENU_PRICE);
	}

	@Test
	@DisplayName("이미지 URL 형식이 잘못되면 ServiceException(DOMAIN_INVALID_MENU_IMAGE_URL)")
	void image_url_invalid() {
		// given
		var menuInfoState = custom(s -> s.customImageUrl("ftp://bad"));

		// when -> then
		assertThatThrownBy(
			() -> Menu.create(menuInfoState, VALID_ORDER_1, VALID_STORE_ID_1, VALID_MENU_CATEGORY_ID_1))
			.isInstanceOf(ServiceException.class)
			.extracting(ex -> ((ServiceException)ex).getErrorCode())
			.isEqualTo(ErrorCode.DOMAIN_INVALID_MENU_IMAGE_URL);
	}

	@Test
	@DisplayName("메뉴 설명, 이미지 URL null은 허용된다")
	void image_url_nullable() {
		// given
		var menuInfoState = custom(s -> {
			s.customDescription(null);
			s.customImageUrl(null);
		});

		// when
		Menu menu = Menu.create(menuInfoState, VALID_ORDER_1, VALID_STORE_ID_1, VALID_MENU_CATEGORY_ID_1);

		// then
		assertSoftly(softly -> {
			softly.assertThat(menu.getMenuInfo().getDescription()).isNull();
			softly.assertThat(menu.getMenuInfo().getImageUrl()).isNull();
		});
	}
}
