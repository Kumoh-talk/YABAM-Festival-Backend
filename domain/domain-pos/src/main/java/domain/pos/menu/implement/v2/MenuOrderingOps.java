package domain.pos.menu.implement.v2;

import java.util.Optional;

import org.springframework.stereotype.Component;

import domain.pos.menu.entity.v2.domain.Menu;
import domain.pos.menu.port.required.MenuCategoryRepository;
import domain.pos.menu.port.required.MenuRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MenuOrderingOps implements OrderingOps<Menu> {
	private final MenuCategoryRepository menuCategoryRepository;
	private final MenuRepository menuRepository;

	@Override
	public void lockGuard(Long guardId) {
		menuCategoryRepository.lock(guardId);
	}

	@Override
	public void bumpGuardVersion(Long guardId) {
		menuCategoryRepository.bumpVersion(guardId);
	}

	@Override
	public Integer readGuardMaxOrder(Long guardId) {
		return menuRepository.readMaxOrder(guardId);
	}

	@Override
	public void updateToTemporaryOrder(Long targetId) {
		menuRepository.updateToTemporaryOrder(targetId, TEMPORARY_ORDER);
	}

	@Override
	public Optional<Menu> updateOrder(Long userId, Long storeId, Long targetId, Integer order) {
		return menuRepository.updateOrder(userId, storeId, targetId, order);
	}

	@Override
	public void incrementOrdersInRange(Long guardId, Integer startOrder, Integer endOrder) {
		menuRepository.incrementOrdersInRange(guardId, startOrder, endOrder);
	}

	@Override
	public void decrementOrdersInRange(Long guardId, Integer startOrder, Integer endOrder) {
		menuRepository.decrementOrdersInRange(guardId, startOrder, endOrder);
	}

	@Override
	public Integer refreshOrder(Long targetId) {
		return menuRepository.refresh(targetId).getOrder();
	}
}
