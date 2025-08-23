package domain.pos.store.port.provided;

import org.springframework.transaction.annotation.Transactional;

import com.vo.UserPassport;

import domain.pos.store.entity.Store;
import domain.pos.store.entity.StoreInfo;

public interface StoreCommand {
	Store createStore(final UserPassport ownerPassport, final StoreInfo createRequestStoreInfo);

	Store updateStoreInfo(
		UserPassport ownerPassport,
		Long queryStoreId,
		StoreInfo modifyStoreInfo);

	void deleteStore(
		UserPassport ownerPassport,
		Long queryStoreId
	);

	void registerStoreDetailImage(
		UserPassport ownerPassport,
		Long queryStoreId,
		String imageUrl
	);

	@Transactional
	void deleteStoreDetailImage(
		UserPassport ownerPassport,
		Long queryStoreId,
		String imageUrl
	);
}
