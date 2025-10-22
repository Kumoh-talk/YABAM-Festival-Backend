package domain.pos.menu.entity.v2;

import static fixtures.menu.v2.MenuFixture.*;
import static fixtures.menu.v2.state.MenuInfoStateTestImpl.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.exception.ErrorCode;
import com.exception.ServiceException;

class MenuUpdateTest {
	@Nested
	@DisplayName("updateMenuInfo 테스트")
	class updateMenuInfoTest {
		@Test
		@DisplayName("메뉴 정보 수정 성공")
		void updateMenuInfo_success() {
			// given
			var menu = VALID_MENU();
			var updateMenuInfo = ANOTHER_VALID_STATE();

			// when
			boolean isUpdate = menu.updateMenuInfo(updateMenuInfo);

			// then
			assertSoftly(softly -> {
				softly.assertThat(isUpdate).isTrue();
				softly.assertThat(menu.getMenuInfo().getName()).isEqualTo(updateMenuInfo.getName());
				softly.assertThat(menu.getMenuInfo().getPrice()).isEqualTo(updateMenuInfo.getPrice());
				softly.assertThat(menu.getMenuInfo().getDescription()).isEqualTo(updateMenuInfo.getDescription());
				softly.assertThat(menu.getMenuInfo().getImageUrl()).isEqualTo(updateMenuInfo.getImageUrl());
			});
		}

		@Test
		@DisplayName("동일한 메뉴 정보로 수정 시도하면 수정되지 않음")
		void update_same_menuInfo() {
			// given
			var menu = VALID_MENU();
			var pastMenuInfo = menu.getMenuInfo();

			// when
			boolean isUpdate = menu.updateMenuInfo(pastMenuInfo);

			// then
			assertSoftly(softly -> {
				softly.assertThat(isUpdate).isFalse();
				softly.assertThat(menu.getMenuInfo()).isSameAs(pastMenuInfo);
			});
		}

		@ParameterizedTest
		@NullAndEmptySource
		@ValueSource(strings = {" ", "   ", "\t", "\n"})
		@DisplayName("이름이 Null 또는 공백이면 ServiceException(DOMAIN_INVALID_MENU_NAME)")
		void name_null_or_blank(String menuName) {
			// given
			var menu = VALID_MENU();
			var updateMenuInfo = custom(s -> s.customName(menuName));

			// when -> then
			assertThatThrownBy(
				() -> menu.updateMenuInfo(updateMenuInfo))
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
			var menu = VALID_MENU();
			var updateMenuInfo = custom(s -> s.customPrice(price));

			// when -> then
			assertThatThrownBy(
				() -> menu.updateMenuInfo(updateMenuInfo))
				.isInstanceOf(ServiceException.class)
				.extracting(ex -> ((ServiceException)ex).getErrorCode())
				.isEqualTo(ErrorCode.DOMAIN_INVALID_MENU_PRICE);
		}

		@Test
		@DisplayName("이미지 URL 형식이 잘못되면 ServiceException(DOMAIN_INVALID_MENU_IMAGE_URL)")
		void image_url_invalid() {
			// given
			var menu = VALID_MENU();
			var updateMenuInfo = custom(s -> s.customImageUrl("ftp://bad"));

			// when -> then
			assertThatThrownBy(
				() -> menu.updateMenuInfo(updateMenuInfo))
				.isInstanceOf(ServiceException.class)
				.extracting(ex -> ((ServiceException)ex).getErrorCode())
				.isEqualTo(ErrorCode.DOMAIN_INVALID_MENU_IMAGE_URL);
		}

		@Test
		@DisplayName("메뉴 설명, 이미지 URL null은 허용된다")
		void image_url_nullable() {
			// given
			var menu = VALID_MENU();
			var updateMenuInfo = custom(s -> {
				s.customDescription(null);
				s.customImageUrl(null);
			});

			// when
			boolean isChanged = menu.updateMenuInfo(updateMenuInfo);

			// then
			assertSoftly(softly -> {
				softly.assertThat(isChanged).isTrue();
				softly.assertThat(menu.getMenuInfo().getDescription()).isNull();
				softly.assertThat(menu.getMenuInfo().getImageUrl()).isNull();
			});
		}
	}

	@Nested
	@DisplayName("updateOrder 테스트")
	class updateOrderTest {
		@Test
		@DisplayName("메뉴 순서 수정 성공")
		void updateOrder_success() {
			// given
			var menu = VALID_MENU();
			var updateOrder = menu.getOrder() + 1;

			// when
			boolean isChanged = menu.updateOrder(updateOrder);

			// then
			assertSoftly(softly -> {
				softly.assertThat(isChanged).isTrue();
				softly.assertThat(menu.getOrder()).isEqualTo(updateOrder);
			});
		}

		@Test
		@DisplayName("동일한 순서로 수정 시도하면 수정되지 않음")
		void updateOrder_same_order() {
			// given
			var menu = VALID_MENU();
			var pastOrder = menu.getOrder();

			// when
			boolean isChanged = menu.updateOrder(pastOrder);

			// then
			assertSoftly(softly -> {
				softly.assertThat(isChanged).isFalse();
				softly.assertThat(menu.getOrder()).isEqualTo(pastOrder);
			});
		}

		@ParameterizedTest
		@NullSource
		@ValueSource(ints = {0, -1, -100})
		@DisplayName("메뉴 순서가 Null 또는 1보다 작으면 ServiceException(DOMAIN_INVALID_MENU_ORDER)")
		void order_must_be_positive(Integer invalidOrder) {
			// given
			var menu = VALID_MENU();

			// when -> then
			assertThatThrownBy(() -> menu.updateOrder(invalidOrder))
				.isInstanceOf(ServiceException.class)
				.extracting(ex -> ((ServiceException)ex).getErrorCode())
				.isEqualTo(ErrorCode.DOMAIN_INVALID_MENU_ORDER);
		}
	}

	@Nested
	@DisplayName("updateIsSoldOut 테스트")
	class updateIsSoldOutTest {
		@Test
		@DisplayName("품절 상태 변경 성공")
		void updateIsSoldOut_success() {
			// given
			var menu = VALID_MENU();
			var pastIsSoldOut = menu.isSoldOut();

			// when
			boolean isChanged = menu.updateIsSoldOut(!pastIsSoldOut);

			// then
			assertSoftly(softly -> {
				softly.assertThat(isChanged).isTrue();
				softly.assertThat(menu.isSoldOut()).isEqualTo(!pastIsSoldOut);
			});
		}

		@Test
		@DisplayName("동일한 품절 상태로 변경 시도하면 변경되지 않음")
		void updateIsSoldOut_same_state() {
			// given
			var menu = VALID_MENU();
			var pastIsSoldOut = menu.isSoldOut();

			// when
			boolean isChanged = menu.updateIsSoldOut(pastIsSoldOut);

			// then
			assertSoftly(softly -> {
				softly.assertThat(isChanged).isFalse();
				softly.assertThat(menu.isSoldOut()).isEqualTo(pastIsSoldOut);
			});
		}
	}

	@Nested
	@DisplayName("updateIsRecommended 테스트")
	class updateIsRecommendedTest {
		@Test
		@DisplayName("추천 상태 변경 성공")
		void updateIsRecommended_success() {
			// given
			var menu = VALID_MENU();
			var pastIsRecommended = menu.isRecommended();

			// when
			boolean isChanged = menu.updateIsRecommended(!pastIsRecommended);

			// then
			assertSoftly(softly -> {
				softly.assertThat(isChanged).isTrue();
				softly.assertThat(menu.isRecommended()).isEqualTo(!pastIsRecommended);
			});
		}

		@Test
		@DisplayName("동일한 추천 상태로 변경 시도하면 변경되지 않음")
		void updateIsRecommended_same_state() {
			// given
			var menu = VALID_MENU();
			var pastIsRecommended = menu.isRecommended();

			// when
			boolean isChanged = menu.updateIsRecommended(pastIsRecommended);

			// then
			assertSoftly(softly -> {
				softly.assertThat(isChanged).isFalse();
				softly.assertThat(menu.isRecommended()).isEqualTo(pastIsRecommended);
			});
		}
	}
}
