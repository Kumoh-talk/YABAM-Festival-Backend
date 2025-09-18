package domain.pos.menu.entity.v2;

import static fixtures.menu.v2.MenuCategoryFixture.*;
import static org.assertj.core.api.SoftAssertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import domain.pos.menu.entity.v2.domain.MenuCategory;

class MenuCategoryUpdateTest {
	@Nested
	@DisplayName("updateName 테스트")
	class updateNameTest {
		@Test
		@DisplayName("메뉴 카테고리 이름 수정 성공")
		void updateNameTest_success() {
			// given
			MenuCategory menuCategory = VALID_CATEGORY();
			String updateName = "디저트";

			// when
			boolean isChanged = menuCategory.updateName(updateName);

			// then
			assertSoftly(softly -> {
				softly.assertThat(isChanged).isTrue();
				softly.assertThat(menuCategory.getName()).isEqualTo(updateName);
			});
		}

		@Test
		@DisplayName("동일한 이름으로 수정 시도하면 수정되지 않음")
		void updateName_same_name() {
			// given
			MenuCategory menuCategory = VALID_CATEGORY();
			String pastName = menuCategory.getName();

			// when
			boolean isChanged = menuCategory.updateName(pastName);

			// then
			assertSoftly(softly -> {
				softly.assertThat(isChanged).isFalse();
				softly.assertThat(menuCategory.getName()).isEqualTo(pastName);
			});
		}

		@ParameterizedTest
		@NullAndEmptySource
		@ValueSource(strings = {" ", "   ", "\t", "\n"})
		@DisplayName("Null 또는 공백으로 수정 시도하면 예외 발생")
		void updateName_null_or_blank(String invalidName) {
			// given
			MenuCategory menuCategory = VALID_CATEGORY();
			String pastName = menuCategory.getName();

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> menuCategory.updateName(invalidName))
					.isInstanceOf(com.exception.ServiceException.class)
					.extracting(ex -> ((com.exception.ServiceException)ex).getErrorCode())
					.isEqualTo(com.exception.ErrorCode.DOMAIN_INVALID_MENU_CATEGORY_NAME);

				softly.assertThat(menuCategory.getName()).isEqualTo(pastName);
			});
		}
	}

	@Nested
	@DisplayName("updateOrder 테스트")
	class updateOrderTest {
		@Test
		@DisplayName("메뉴 카테고리 순서 수정 성공")
		void updateOrder_success() {
			// given
			MenuCategory menuCategory = VALID_CATEGORY();
			Integer updateOrder = 2;

			// when
			boolean isChanged = menuCategory.updateOrder(updateOrder);

			// then
			assertSoftly(softly -> {
				softly.assertThat(isChanged).isTrue();
				softly.assertThat(menuCategory.getOrder()).isEqualTo(updateOrder);
			});
		}

		@Test
		@DisplayName("동일한 순서로 수정 시도하면 수정되지 않음")
		void updateOrder_same_order() {
			// given
			MenuCategory menuCategory = VALID_CATEGORY();
			Integer pastOrder = menuCategory.getOrder();

			// when
			boolean isChanged = menuCategory.updateOrder(pastOrder);

			// then
			assertSoftly(softly -> {
				softly.assertThat(isChanged).isFalse();
				softly.assertThat(menuCategory.getOrder()).isEqualTo(pastOrder);
			});
		}

		@ParameterizedTest
		@NullSource
		@ValueSource(ints = {0, -1, -100})
		@DisplayName("메뉴 카테고리 순서가 Null 또는 1보다 작으면 ServiceException(DOMAIN_INVALID_MENU_CATEGORY_ORDER)")
		void order_must_be_positive(Integer invalidOrder) {
			// given
			MenuCategory menuCategory = VALID_CATEGORY();
			Integer pastOrder = menuCategory.getOrder();

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> menuCategory.updateOrder(invalidOrder))
					.isInstanceOf(com.exception.ServiceException.class)
					.extracting(ex -> ((com.exception.ServiceException)ex).getErrorCode())
					.isEqualTo(com.exception.ErrorCode.DOMAIN_INVALID_MENU_CATEGORY_ORDER);

				softly.assertThat(menuCategory.getOrder()).isEqualTo(pastOrder);
			});
		}
	}
}
