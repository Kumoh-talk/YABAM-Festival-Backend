package domain.pos.store.port.required;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.vo.UserPassport;

import domain.pos.store.entity.Store;
import domain.pos.store.entity.StoreInfo;
import domain.pos.store.entity.dto.StoreHeadDto;

@Repository
public interface StoreRepository {
	@Deprecated
	Long createStore(UserPassport userPassport, StoreInfo createRequestStoreInfo);

	Optional<Store> findStoreByStoreId(Long storeId);

	@Deprecated
	Store changeStoreInfo(Store previousStore, StoreInfo requestChangeStoreInfo);

	void deleteStore(Store previousStore);

	@Deprecated
	Store changeStoreOpenStatus(Store previousStore);

	@Deprecated
	boolean isExistsById(Long storeId);

	@Deprecated
	void postDetailImage(Store previousStore, String imageUrl);

	@Deprecated
	boolean isExistsImageUrl(Long storeId, String imageUrl);

	@Deprecated
	void deleteDetailImage(Store previousStore, String imageUrl);

	Slice<StoreHeadDto> findStoresCursorOrderByCreated(Long lastStoreId, int size);

	List<Store> findMyStores(Long userId);

	@Deprecated
	Optional<Store> findStoreByStoreIdWithLock(Long queryStoreId);

	Store save(Store store);

	boolean isExistsByStoreIdAndUserId(Long queryStoreId, Long userId);
}
