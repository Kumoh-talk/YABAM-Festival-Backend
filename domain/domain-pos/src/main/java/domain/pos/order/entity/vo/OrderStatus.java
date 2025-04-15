package domain.pos.order.entity.vo;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserRole;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum OrderStatus {
	ORDERED {
		@Override
		public void receivedOrder(Long orderId, UserRole requesterRole) {
			if (requesterRole.equals(UserRole.ROLE_OWNER)) {
				log.info("주문이 접수되었습니다 : orderId={}", orderId);
			} else {
				log.warn("주문을 접수할 수 있는 권한이 없습니다 : orderId={}", orderId);
				throw new ServiceException(ErrorCode.ORDER_ACCESS_DENIED);
			}
		}

		@Override
		public void cancelledOrder(Long orderId, UserRole requesterRole) {
			if (requesterRole.equals(UserRole.ROLE_OWNER)) {
				log.info("점주에 의해 주문이 취소되었습니다 : orderId={}", orderId);
			} else {
				log.info("고객에 의해 주문이 취소되었습니다 : orderId={}", orderId);
			}
		}

		@Override
		public void completedOrder(Long orderId, UserRole requesterRole) {
			log.warn("주문이 완료될 수 없는 상태입니다 : orderId={}", orderId);
			throw new ServiceException(ErrorCode.INVALID_STATE_TRANSITION);
		}
	},
	RECEIVED {
		@Override
		public void receivedOrder(Long orderId, UserRole requesterRole) {
			log.warn("이미 점주가 접수한 주문입니다 : orderId={}", orderId);
			throw new ServiceException(ErrorCode.ALREADY_RECEIVED_ORDER);
		}

		@Override
		public void cancelledOrder(Long orderId, UserRole requesterRole) {
			if (requesterRole.equals(UserRole.ROLE_OWNER)) {
				log.info("점주에 의해 주문이 취소되었습니다 : orderId={}", orderId);
			} else {
				log.warn("이미 점주가 접수한 주문입니다. : orderId={}", orderId);
				throw new ServiceException(ErrorCode.ALREADY_RECEIVED_ORDER);
			}
		}

		@Override
		public void completedOrder(Long orderId, UserRole requesterRole) {
			if (requesterRole.equals(UserRole.ROLE_OWNER)) {
				log.info("주문 서빙이 완료되었습니다 : orderId={}", orderId);
			} else {
				log.warn("주문 서빙을 완료할 수 있는 권한이 없습니다 : orderId={}", orderId);
				throw new ServiceException(ErrorCode.ORDER_ACCESS_DENIED);
			}
		}
	},
	CANCELLED {
		@Override
		public void receivedOrder(Long orderId, UserRole requesterRole) {
			log.warn("이미 취소된 주문입니다 : orderId={}", orderId);
			throw new ServiceException(ErrorCode.ALREADY_CANCELLED_ORDER);
		}

		@Override
		public void cancelledOrder(Long orderId, UserRole requesterRole) {
			log.warn("이미 취소된 주문입니다 : orderId={}", orderId);
			throw new ServiceException(ErrorCode.ALREADY_CANCELLED_ORDER);
		}

		@Override
		public void completedOrder(Long orderId, UserRole requesterRole) {
			log.warn("이미 취소된 주문입니다 : orderId={}", orderId);
			throw new ServiceException(ErrorCode.ALREADY_CANCELLED_ORDER);
		}
	},
	COMPLETED {
		@Override
		public void receivedOrder(Long orderId, UserRole requesterRole) {
			log.warn("이미 완료된 주문입니다 : orderId={}", orderId);
			throw new ServiceException(ErrorCode.ALREADY_COMPLETED_ORDER);
		}

		@Override
		public void cancelledOrder(Long orderId, UserRole requesterRole) {
			if (requesterRole.equals(UserRole.ROLE_OWNER)) {
				log.info("점주에 의해 주문이 취소되었습니다 : orderId={}", orderId);
			} else {
				log.warn("이미 완료된 주문입니다 : orderId={}", orderId);
				throw new ServiceException(ErrorCode.ALREADY_COMPLETED_ORDER);
			}
		}

		@Override
		public void completedOrder(Long orderId, UserRole requesterRole) {
			log.warn("이미 완료된 주문입니다 : orderId={}", orderId);
			throw new ServiceException(ErrorCode.ALREADY_COMPLETED_ORDER);
		}
	};

	public abstract void receivedOrder(Long orderId, UserRole requesterRole);

	public abstract void cancelledOrder(Long orderId, UserRole requesterRole);

	public abstract void completedOrder(Long orderId, UserRole requesterRole);
}
