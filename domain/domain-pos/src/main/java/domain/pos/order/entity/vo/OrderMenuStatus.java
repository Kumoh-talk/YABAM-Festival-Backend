package domain.pos.order.entity.vo;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserRole;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum OrderMenuStatus {
	ORDERED {
		@Override
		public void reCookingOrderMenu(Long orderMenuId, UserRole requesterRole) {
			if (requesterRole.equals(UserRole.ROLE_OWNER)) {
				log.info("점주에 의해 주문 메뉴가 조리중이 되었습니다 : orderMenuId={}", orderMenuId);
			} else {
				log.warn("주문 메뉴를 조리 상태로 만들 권한이 없습니다 : orderMenuId={}", orderMenuId);
				throw new ServiceException(ErrorCode.TRANSFER_INVALID_ROLE);
			}
		}

		@Override
		public void cancelOrderMenu(Long orderMenuId, UserRole requesterRole) {
			if (requesterRole.equals(UserRole.ROLE_OWNER)) {
				log.info("점주에 의해 주문 메뉴가 취소되었습니다 : orderMenuId={}", orderMenuId);
			} else {
				log.info("고객에 의해 주문 메뉴가 취소되었습니다 : orderMenuId={}", orderMenuId);
			}
		}

		@Override
		public void completeOrderMenu(Long orderMenuId, UserRole requesterRole) {
			if (requesterRole.equals(UserRole.ROLE_OWNER)) {
				log.info("점주에 의해 주문 메뉴가 완료되었습니다 : orderMenuId={}", orderMenuId);
			} else {
				log.warn("주문 메뉴를 완료 상태로 만들 권한이 없습니다 : orderMenuId={}", orderMenuId);
				throw new ServiceException(ErrorCode.TRANSFER_INVALID_ROLE);
			}
		}
	},
	COOKING {
		@Override
		public void reCookingOrderMenu(Long orderMenuId, UserRole requesterRole) {
			log.warn("이미 조리중인 메뉴입니다 : orderMenuId={}", orderMenuId);
			throw new ServiceException(ErrorCode.ALREADY_COOKING_ORDER_MENU);
		}

		@Override
		public void cancelOrderMenu(Long orderMenuId, UserRole requesterRole) {
			if (requesterRole.equals(UserRole.ROLE_OWNER)) {
				log.info("점주에 의해 주문 메뉴가 취소되었습니다 : orderMenuId={}", orderMenuId);
			} else {
				log.warn("이미 조리중인 메뉴입니다 : orderMenuId={}", orderMenuId);
				throw new ServiceException(ErrorCode.ALREADY_COOKING_ORDER_MENU);
			}
		}

		@Override
		public void completeOrderMenu(Long orderMenuId, UserRole requesterRole) {
			if (requesterRole.equals(UserRole.ROLE_OWNER)) {
				log.info("점주에 의해 주문 메뉴가 완료되었습니다 : orderMenuId={}", orderMenuId);
			} else {
				log.warn("주문 메뉴를 완료 상태로 만들 권한이 없습니다 : orderMenuId={}", orderMenuId);
				throw new ServiceException(ErrorCode.TRANSFER_INVALID_ROLE);
			}
		}
	},
	CANCELED {
		@Override
		public void reCookingOrderMenu(Long orderMenuId, UserRole requesterRole) {
			if (requesterRole.equals(UserRole.ROLE_OWNER)) {
				log.info("점주에 의해 주문 메뉴가 조리중이 되었습니다 : orderMenuId={}", orderMenuId);
			} else {
				log.warn("주문 메뉴를 조리 상태로 만들 권한이 없습니다 : orderMenuId={}", orderMenuId);
				throw new ServiceException(ErrorCode.TRANSFER_INVALID_ROLE);
			}
		}

		@Override
		public void cancelOrderMenu(Long orderMenuId, UserRole requesterRole) {
			log.warn("이미 취소된 메뉴입니다 : orderMenuId={}", orderMenuId);
			throw new ServiceException(ErrorCode.ALREADY_CANCELED_ORDER_MENU);
		}

		@Override
		public void completeOrderMenu(Long orderMenuId, UserRole requesterRole) {
			log.warn("이미 취소된 메뉴입니다 : orderMenuId={}", orderMenuId);
			throw new ServiceException(ErrorCode.ALREADY_CANCELED_ORDER_MENU);
		}
	},
	COMPLETED {
		@Override
		public void reCookingOrderMenu(Long orderMenuId, UserRole requesterRole) {
			if (requesterRole.equals(UserRole.ROLE_OWNER)) {
				log.info("점주에 의해 주문 메뉴가 조리중이 되었습니다 : orderMenuId={}", orderMenuId);
			} else {
				log.warn("주문 메뉴를 조리 상태로 만들 권한이 없습니다 : orderMenuId={}", orderMenuId);
				throw new ServiceException(ErrorCode.TRANSFER_INVALID_ROLE);
			}
		}

		@Override
		public void cancelOrderMenu(Long orderMenuId, UserRole requesterRole) {
			log.warn("이미 완료된 메뉴입니다 : orderMenuId={}", orderMenuId);
			throw new ServiceException(ErrorCode.ALREADY_COMPLETED_ORDER_MENU);
		}

		@Override
		public void completeOrderMenu(Long orderMenuId, UserRole requesterRole) {
			log.warn("이미 완료된 메뉴입니다 : orderMenuId={}", orderMenuId);
			throw new ServiceException(ErrorCode.ALREADY_COMPLETED_ORDER_MENU);
		}
	};

	public abstract void reCookingOrderMenu(Long orderMenuId, UserRole requesterRole);

	public abstract void cancelOrderMenu(Long orderMenuId, UserRole requesterRole);

	public abstract void completeOrderMenu(Long orderMenuId, UserRole requesterRole);

}
