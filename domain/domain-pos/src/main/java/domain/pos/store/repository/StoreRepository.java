package domain.pos.store.repository;

import java.util.Optional;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import domain.pos.member.entity.UserPassport;
import domain.pos.store.entity.Store;
import domain.pos.store.entity.StoreInfo;
import domain.pos.store.entity.dto.StoreHeadDto;

@Repository
public interface StoreRepository {
	Long createStore(UserPassport userPassport, StoreInfo createRequestStoreInfo);

	Optional<Store> findStoreByStoreId(Long storeId);

	Store changeStoreInfo(Store previousStore, StoreInfo requestChangeStoreInfo);

	void deleteStore(Store previousStore);

	Store changeStoreOpenStatus(Store previousStore);

	boolean isExistsById(Long storeId);

	void postDetailImage(Store previousStore, String imageUrl);

	boolean isExistsImageUrl(Long storeId, String imageUrl);

	void deleteDetailImage(Store previousStore, String imageUrl);

	Slice<StoreHeadDto> findStoresCursorOrderByReviewCount(Long cursorReviewCount, Long cursorStoreId, int size);
}
