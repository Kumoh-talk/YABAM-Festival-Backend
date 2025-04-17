package domain.pos.order.implement;

import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserPassport;
import domain.pos.order.entity.Order;
import domain.pos.order.entity.vo.OrderStatus;
import domain.pos.order.repository.OrderRepository;
import fixtures.member.UserFixture;
import fixtures.order.OrderFixture;

@ExtendWith(MockitoExtension.class)
public class OrderWriterTest {
	@Mock
	private OrderRepository orderRepository;
	@InjectMocks
	private OrderWriter orderWriter;

	@Nested
	@DisplayName("주문 상태 ORDERED로 변경")
	class patchOrderStatusToORDERED {
		private final OrderStatus orderStatus = OrderStatus.ORDERED;

		@ParameterizedTest
		@EnumSource(OrderStatus.class)
		void ALL_TO_ORDERED_실패(OrderStatus initialStatus) {
			// given
			Order order = OrderFixture.CREATE_ORDER_WITH_STATUS(initialStatus);
			UserPassport userPassport = UserFixture.GENERAL_USER_PASSPORT();

			// when, then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> orderWriter.patchOrderStatus(order, orderStatus, userPassport.getUserRole()))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_STATE_TRANSITION);
			});
		}
	}

	@Nested
	@DisplayName("주문 상태 RECEIVED로 변경")
	class patchOrderStatusToRECEIVED {
		private final OrderStatus orderStatus = OrderStatus.RECEIVED;

		@Test
		void ORDERED_TO_RECEIVED_성공() {
			// given
			Order order = OrderFixture.ORDERED_ORDER();
			UserPassport userPassport = UserFixture.OWNER_USER_PASSPORT();

			// when
			orderWriter.patchOrderStatus(order, orderStatus, userPassport.getUserRole());

			// then
			verify(orderRepository).patchOrderStatus(order, orderStatus);
		}

		@Test
		void ORDERED_TO_RECEIVED_권한_실패() {
			// given
			Order order = OrderFixture.ORDERED_ORDER();
			UserPassport userPassport = UserFixture.GENERAL_USER_PASSPORT();

			// when, then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> orderWriter.patchOrderStatus(order, orderStatus, userPassport.getUserRole()))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_ACCESS_DENIED);

				verify(orderRepository, never()).patchOrderStatus(order, orderStatus);
			});
		}

		@Test
		void RECEIVED_TO_RECEIVED_실패() {
			// given
			Order order = OrderFixture.RECEIVED_ORDER();
			UserPassport userPassport = UserFixture.OWNER_USER_PASSPORT();

			// when, then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> orderWriter.patchOrderStatus(order, orderStatus, userPassport.getUserRole()))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_RECEIVED_ORDER);

				verify(orderRepository, never()).patchOrderStatus(order, orderStatus);
			});
		}

		@Test
		void CANCELLED_TO_RECEIVED_실패() {
			// given
			Order order = OrderFixture.CANCELLED_ORDER();
			UserPassport userPassport = UserFixture.OWNER_USER_PASSPORT();

			// when, then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> orderWriter.patchOrderStatus(order, orderStatus, userPassport.getUserRole()))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_CANCELLED_ORDER);

				verify(orderRepository, never()).patchOrderStatus(order, orderStatus);
			});
		}

		@Test
		void COMPLETED_TO_RECEIVED_실패() {
			// given
			Order order = OrderFixture.COMPLETED_ORDER();
			UserPassport userPassport = UserFixture.OWNER_USER_PASSPORT();

			// when, then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> orderWriter.patchOrderStatus(order, orderStatus, userPassport.getUserRole()))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_COMPLETED_ORDER);

				verify(orderRepository, never()).patchOrderStatus(order, orderStatus);
			});
		}
	}

	@Nested
	@DisplayName("주문 상태 CANCELLED로 변경")
	class patchOrderStatusToCANCELLED {
		private final OrderStatus orderStatus = OrderStatus.CANCELLED;

		@Test
		void ORDERED_TO_CANCELLED_점주_성공() {
			// given
			Order order = OrderFixture.ORDERED_ORDER();
			UserPassport userPassport = UserFixture.OWNER_USER_PASSPORT();

			// when
			orderWriter.patchOrderStatus(order, orderStatus, userPassport.getUserRole());

			// then
			verify(orderRepository).patchOrderStatus(order, orderStatus);
		}

		@Test
		void ORDERED_TO_CANCELLED_고객_성공() {
			// given
			Order order = OrderFixture.ORDERED_ORDER();
			UserPassport userPassport = UserFixture.GENERAL_USER_PASSPORT();

			// when
			orderWriter.patchOrderStatus(order, orderStatus, userPassport.getUserRole());

			// then
			verify(orderRepository).patchOrderStatus(order, orderStatus);
		}

		@Test
		void RECEIVED_TO_CANCELLED_점주_성공() {
			// given
			Order order = OrderFixture.RECEIVED_ORDER();
			UserPassport userPassport = UserFixture.OWNER_USER_PASSPORT();

			// when
			orderWriter.patchOrderStatus(order, orderStatus, userPassport.getUserRole());

			// then
			verify(orderRepository).patchOrderStatus(order, orderStatus);
		}

		@Test
		void RECEIVED_TO_CANCELLED_고객_실패() {
			// given
			Order order = OrderFixture.RECEIVED_ORDER();
			UserPassport userPassport = UserFixture.GENERAL_USER_PASSPORT();

			// when, then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> orderWriter.patchOrderStatus(order, orderStatus, userPassport.getUserRole()))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_RECEIVED_ORDER);

				verify(orderRepository, never()).patchOrderStatus(order, orderStatus);
			});
		}

		@Test
		void CANCELLED_TO_CANCELLED_실패() {
			// given
			Order order = OrderFixture.CANCELLED_ORDER();
			UserPassport userPassport = UserFixture.OWNER_USER_PASSPORT();

			// when, then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> orderWriter.patchOrderStatus(order, orderStatus, userPassport.getUserRole()))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_CANCELLED_ORDER);

				verify(orderRepository, never()).patchOrderStatus(order, orderStatus);
			});
		}

		@Test
		void COMPLETED_TO_CANCELLED_점주_성공() {
			// given
			Order order = OrderFixture.COMPLETED_ORDER();
			UserPassport userPassport = UserFixture.OWNER_USER_PASSPORT();

			// when
			orderWriter.patchOrderStatus(order, orderStatus, userPassport.getUserRole());

			// then
			verify(orderRepository).patchOrderStatus(order, orderStatus);
		}

		@Test
		void COMPLETED_TO_CANCELLED_고객_실패() {
			// given
			Order order = OrderFixture.COMPLETED_ORDER();
			UserPassport userPassport = UserFixture.GENERAL_USER_PASSPORT();

			// when -> then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(() -> orderWriter.patchOrderStatus(order, orderStatus,
						userPassport.getUserRole()))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_COMPLETED_ORDER);
			});
		}
	}
}
