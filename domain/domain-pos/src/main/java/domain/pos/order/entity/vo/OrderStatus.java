package domain.pos.order.entity.vo;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserRole;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum OrderStatus {
	ORDERED {
		@Override
		public void receiveOrder(Long orderId, UserRole requesterRole) {
			if (requesterRole.equals(UserRole.ROLE_OWNER)) {
				log.info("주문이 접수되었습니다 : orderId={}", orderId);
			} else {
				log.warn("주문을 접수할 수 있는 권한이 없습니다 : orderId={}", orderId);
				throw new ServiceException(ErrorCode.ORDER_ACCESS_DENIED);
			}
		}

		@Override
		public void cancelOrder(Long orderId, UserRole requesterRole) {
			if (requesterRole.equals(UserRole.ROLE_OWNER)) {
				log.info("점주에 의해 주문이 취소되었습니다 : orderId={}", orderId);
			} else {
				log.info("고객에 의해 주문이 취소되었습니다 : orderId={}", orderId);
			}
		}

		@Override
		public OrderMenuStatus transferOrderMenuStatus() {
			return OrderMenuStatus.ORDERED;
		}
	},
	RECEIVED {
		@Override
		public void receiveOrder(Long orderId, UserRole requesterRole) {
			log.warn("이미 점주가 접수한 주문입니다 : orderId={}", orderId);
			throw new ServiceException(ErrorCode.ALREADY_RECEIVED_ORDER);
		}

		@Override
		public void cancelOrder(Long orderId, UserRole requesterRole) {
			if (requesterRole.equals(UserRole.ROLE_OWNER)) {
				log.info("점주에 의해 주문이 취소되었습니다 : orderId={}", orderId);
			} else {
				log.warn("이미 점주가 접수한 주문입니다. : orderId={}", orderId);
				throw new ServiceException(ErrorCode.ALREADY_RECEIVED_ORDER);
			}
		}

		@Override
		public OrderMenuStatus transferOrderMenuStatus() {
			return OrderMenuStatus.COOKING;
		}
	},
	CANCELED {
		@Override
		public void receiveOrder(Long orderId, UserRole requesterRole) {
			log.warn("이미 취소된 주문입니다 : orderId={}", orderId);
			throw new ServiceException(ErrorCode.ALREADY_CANCELED_ORDER);
		}

		@Override
		public void cancelOrder(Long orderId, UserRole requesterRole) {
			log.warn("이미 취소된 주문입니다 : orderId={}", orderId);
			throw new ServiceException(ErrorCode.ALREADY_CANCELED_ORDER);
		}

		@Override
		public OrderMenuStatus transferOrderMenuStatus() {
			return OrderMenuStatus.CANCELED;
		}

	},
	COMPLETED {
		@Override
		public void receiveOrder(Long orderId, UserRole requesterRole) {
			log.warn("이미 완료된 주문입니다 : orderId={}", orderId);
			throw new ServiceException(ErrorCode.ALREADY_COMPLETED_ORDER);
		}

		@Override
		public void cancelOrder(Long orderId, UserRole requesterRole) {
			if (requesterRole.equals(UserRole.ROLE_OWNER)) {
				log.info("점주에 의해 주문이 취소되었습니다 : orderId={}", orderId);
			} else {
				log.warn("이미 완료된 주문입니다 : orderId={}", orderId);
				throw new ServiceException(ErrorCode.ALREADY_COMPLETED_ORDER);
			}
		}

		@Override
		public OrderMenuStatus transferOrderMenuStatus() {
			return OrderMenuStatus.COMPLETED;
		}
	};

	public abstract void receiveOrder(Long orderId, UserRole requesterRole);

	public abstract void cancelOrder(Long orderId, UserRole requesterRole);

	public abstract OrderMenuStatus transferOrderMenuStatus();
}
