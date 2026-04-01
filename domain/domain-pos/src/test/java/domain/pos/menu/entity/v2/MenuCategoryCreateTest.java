package domain.pos.menu.entity.v2;

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

import domain.pos.menu.entity.v2.domain.MenuCategory;

class MenuCategoryCreateTest {
	@Test
	@DisplayName("메뉴 카테고리 생성 성공")
	void create_success() {
		String name = "음료";
		Integer order = 1;
		Long storeId = 2L;

		MenuCategory menuCategory = MenuCategory.create(name, order, storeId);

		assertSoftly(softly -> {
			softly.assertThat(menuCategory.getId()).isNull();
			softly.assertThat(menuCategory.getName()).isEqualTo(name);
			softly.assertThat(menuCategory.getOrder()).isEqualTo(order);
			softly.assertThat(menuCategory.getStoreId()).isEqualTo(storeId);
		});
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "   ", "\t", "\n"})
	@DisplayName("메뉴 이름이 Null 또는 공백이면 ServiceException(DOMAIN_INVALID_MENU_CATEGORY_NAME)")
	void name_null_or_blank(String name) {
		// when -> then
		assertThatThrownBy(() -> MenuCategory.create(name, 1, 1L))
			.isInstanceOf(ServiceException.class)
			.extracting(ex -> ((ServiceException)ex).getErrorCode())
			.isEqualTo(ErrorCode.DOMAIN_INVALID_MENU_CATEGORY_NAME);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(ints = {0, -1, -100})
	@DisplayName("메뉴 순서가 Null 또는 1보다 작으면 ServiceException(DOMAIN_INVALID_MENU_CATEGORY_ORDER)")
	void order_must_be_positive(Integer invalidOrder) {
		// when -> then
		assertThatThrownBy(() -> MenuCategory.create("음료", invalidOrder, 1L))
			.isInstanceOf(ServiceException.class)
			.extracting(ex -> ((ServiceException)ex).getErrorCode())
			.isEqualTo(ErrorCode.DOMAIN_INVALID_MENU_CATEGORY_ORDER);
	}

	@Test
	@DisplayName("storeId가 null이면 NullPointerException")
	void storeId_null() {
		// when -> then
		assertThatThrownBy(() -> MenuCategory.create("음료", 1, null))
			.isInstanceOf(NullPointerException.class);
	}
}
