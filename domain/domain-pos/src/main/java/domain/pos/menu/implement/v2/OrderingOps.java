package domain.pos.menu.implement.v2;

import java.util.Optional;

public interface OrderingOps<T> {
	Integer TEMPORARY_ORDER = 0;

	void lockGuard(Long guardId);

	void bumpGuardVersion(Long guardId);

	Integer readGuardMaxOrder(Long guardId);

	void updateToTemporaryOrder(Long targetId);

	Optional<T> updateOrder(Long userId, Long storeId, Long targetId, Integer order);

	void incrementOrdersInRange(Long guardId, Integer startOrder, Integer endOrder);

	void decrementOrdersInRange(Long guardId, Integer startOrder, Integer endOrder);

	Integer refreshOrder(Long targetId);

}
