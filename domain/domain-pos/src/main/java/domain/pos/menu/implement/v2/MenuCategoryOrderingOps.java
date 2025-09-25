package domain.pos.menu.implement.v2;

import java.util.Optional;

import org.springframework.stereotype.Component;

import domain.pos.menu.entity.v2.domain.MenuCategory;
import domain.pos.menu.port.required.MenuCategoryRepository;
import domain.pos.store.port.required.StoreRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MenuCategoryOrderingOps implements OrderingOps<MenuCategory> {
	private final StoreRepository storeRepository;
	private final MenuCategoryRepository menuCategoryRepository;

	@Override
	public void lockGuard(Long guardId) {
		storeRepository.findStoreByStoreIdWithLock(guardId);
	}

	@Override
	public void bumpGuardVersion(Long guardId) {
	}

	@Override
	public Integer readGuardMaxOrder(Long guardId) {
		return menuCategoryRepository.readMaxOrder(guardId);
	}

	@Override
	public void updateToTemporaryOrder(Long targetId) {
		menuCategoryRepository.updateToTemporaryOrder(targetId, TEMPORARY_ORDER);
	}

	@Override
	public Optional<MenuCategory> updateOrder(Long userId, Long storeId, Long targetId, Integer order) {
		return menuCategoryRepository.updateOrder(userId, storeId, targetId, order);
	}

	@Override
	public void incrementOrdersInRange(Long guardId, Integer startOrder, Integer endOrder) {
		menuCategoryRepository.incrementOrdersInRange(guardId, startOrder, endOrder);
	}

	@Override
	public void decrementOrdersInRange(Long guardId, Integer startOrder, Integer endOrder) {
		menuCategoryRepository.decrementOrdersInRange(guardId, startOrder, endOrder);
	}

	@Override
	public Integer refreshOrder(Long targetId) {
		MenuCategory refreshCategory = menuCategoryRepository.refresh(targetId);
		return refreshCategory.getOrder();
	}
}
