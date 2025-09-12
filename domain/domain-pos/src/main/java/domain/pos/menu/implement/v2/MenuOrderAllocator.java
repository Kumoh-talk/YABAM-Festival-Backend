package domain.pos.menu.implement.v2;

import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.menu.entity.v2.domain.Menu;
import domain.pos.menu.port.required.MenuCategoryRepository;
import domain.pos.menu.port.required.MenuRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MenuOrderAllocator {
	private final MenuRepository menuRepository;
	private final MenuCategoryRepository menuCategoryRepository;

	public static final Integer TEMPORARY_ORDER = 0;

	public Integer allocateNewMenuOrder(Long menuCategoryId) {
		// 카테고리 내 마지막 순서 할당을 위한 카테고리 락 -> 메뉴 생성 요청이 여러개 올 경우 생기는 동시성 문제 해결
		menuCategoryRepository.lockMenuCategory(menuCategoryId);
		return menuRepository.readMaxMenuOrder(menuCategoryId) + 1;
	}

	// 메뉴 순서 변경 시, 변경 전 순서 ~ 변경 후 순서 사이의 메뉴 순서 재배치 로직
	public Menu relocationOrders(Menu updatedMenu, Integer previousOrder) {
		Long menuCategoryId = updatedMenu.getMenuCategoryId();
		Integer updatedOrder = updatedMenu.getOrder();

		// 동일 카테고리 내에 여러 메뉴 순서가 수정을 위한 카테고리 락 -> 동일 카테고리 메뉴 순서가 다른 요청에 의해 엉키지 않도록
		menuCategoryRepository.lockMenuCategory(menuCategoryId);

		Integer maxOrder = menuRepository.readMaxMenuOrder(menuCategoryId);
		if (updatedOrder > maxOrder) {
			throw new ServiceException(ErrorCode.DOMAIN_INVALID_MENU_ORDER);
		}

		// 범위 내 메뉴들 순서 재배치 전, 주된 변경 메뉴에게 임시 순서 부여 -> unique 제약 조건 위반 방지
		menuRepository.updateTemporaryOrder(updatedMenu.getId(), TEMPORARY_ORDER);
		// TODO : dirty checking이면 update 순서 신경써야함

		if (previousOrder < updatedOrder) {
			// 변경 전 순서가 변경 후 순서보다 작으면 범위 내 메뉴들의 순서를 -1 시켜야한다.
			menuRepository.decrementMenuOrdersInRange(menuCategoryId, previousOrder + 1, updatedOrder);
		} else {
			// 변경 전 순서가 변경 후 순서보다 크면 범위 내 메뉴들의 순서를 +1 시켜야한다.
			menuRepository.incrementMenuOrdersInRange(menuCategoryId, updatedOrder, previousOrder - 1);
		}

		menuCategoryRepository.bumpVersion(menuCategoryId);

		// 주된 변경 메뉴에게 최종 순서 부여
		return menuRepository.updateOrder(updatedMenu.getId(), updatedOrder);
	}

	public void delete(Menu menu) {
		Long menuCategoryId = menu.getMenuCategoryId();

		// 카테고리 내 메뉴 순서 재배치를 위한 카테고리 락 -> 메뉴 생성 및 삭제 요청이 여러개 올 경우 생기는 동시성 문제 해결
		menuCategoryRepository.lockMenuCategory(menuCategoryId);

		// 락 전 조회한 삭제 menu의 최신 order를 가져오기 위한 refresh
		Menu refreshMenu = menuRepository.refresh(menu);
		Integer deleteOrder = refreshMenu.getOrder();

		// 삭제 menu에게 null 순서 부여 -> unique 제약 조건 위반 방지
		menuRepository.updateOrder(menu.getId(), null);

		menuRepository.deleteMenu(menu.getId());

		// 삭제 menu order보다 큰 order를 가지는 메뉴들의 순서를 -1
		menuRepository.decrementMenuOrdersInRange(menuCategoryId, deleteOrder + 1, Integer.MAX_VALUE);

		menuCategoryRepository.bumpVersion(menuCategoryId);
	}
}
