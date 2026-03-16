package domain.pos.menu.implement.v2;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderAllocator<T> {
	private final OrderingOps<T> orderingOps;

	// 타겟 : 순서 변경 주체, 가드 : 타겟이 속한 동일 순서 그룹(카테고리, 가게 등)
	public Integer allocateNewOrder(Long guardId) {
		// 마지막 순서 할당을 위한 가드 락 -> 타겟 생성 요청이 여러개 올 경우 생기는 동시성 문제 해결
		orderingOps.lockGuard(guardId);
		return orderingOps.readGuardMaxOrder(guardId) + 1;
	}

	// 순서 변경 시, 변경 전 순서 ~ 변경 후 순서 사이의 순서 재배치 로직
	public Optional<T> relocationOrders(Long userId, Long storeId,
			Long guardId, Long targetId, Integer updatedOrder, Integer previousOrder) {
		// 동일 가드 내에 여러 타겟 순서 수정을 위한 가드 락 -> 동일 가드의 타겟 순서가 다른 요청에 의해 엉키지 않도록
		orderingOps.lockGuard(guardId);

		Integer maxOrder = orderingOps.readGuardMaxOrder(guardId);
		if (updatedOrder > maxOrder) {
			throw new ServiceException(ErrorCode.DOMAIN_INVALID_MENU_ORDER);
		}

		// 범위 내 타겟들 순서 재배치 전, 주된 변경 타겟에게 임시 순서 부여 -> unique 제약 조건 위반 방지
		orderingOps.updateToTemporaryOrder(targetId);
		// TODO : dirty checking이면 update 순서 신경써야함

		if (previousOrder < updatedOrder) {
			// 변경 전 순서가 변경 후 순서보다 작으면 범위 내 타겟들의 순서를 -1 시켜야한다.
			orderingOps.decrementOrdersInRange(guardId, previousOrder + 1, updatedOrder);
		} else {
			// 변경 전 순서가 변경 후 순서보다 크면 범위 내 타겟들의 순서를 +1 시켜야한다.
			orderingOps.incrementOrdersInRange(guardId, updatedOrder, previousOrder - 1);
		}

		orderingOps.bumpGuardVersion(guardId);

		// 주된 변경 타겟에게 최종 순서 부여
		return orderingOps.updateOrder(userId, storeId, targetId, updatedOrder);
	}

	public void deleteOrder(Long userId, Long storeId, Long guardId, Long targetId) {
		// 가드 내 타겟 순서 재배치를 위한 가드 락 -> 타겟 생성 및 삭제 요청이 여러개 올 경우 생기는 동시성 문제 해결
		orderingOps.lockGuard(guardId);

		// 락 전 조회한 삭제 타겟의 최신 order를 가져오기 위한 refresh
		Integer deleteOrder = orderingOps.refreshOrder(targetId);

		// 삭제 타겟에게 null 순서 부여 -> unique 제약 조건 위반 방지
		orderingOps.updateOrder(userId, storeId, targetId, null);

		// 삭제 타겟 순서보다 큰 순서를 가지는 메뉴들의 순서를 -1
		orderingOps.decrementOrdersInRange(guardId, deleteOrder + 1, Integer.MAX_VALUE);

		orderingOps.bumpGuardVersion(guardId);
	}
}
