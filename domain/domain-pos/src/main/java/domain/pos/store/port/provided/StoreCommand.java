package domain.pos.store.port.provided;

import org.springframework.transaction.annotation.Transactional;

import com.vo.UserPassport;

import domain.pos.store.entity.Store;
import domain.pos.store.entity.StoreInfo;

public interface StoreCommand {
	Store createStore(final UserPassport ownerPassport, final StoreInfo createRequestStoreInfo);

	@Transactional
	Store updateStoreInfo(
		UserPassport ownerPassport,
		Long queryStoreId,
		StoreInfo modifyStoreInfo);

	@Transactional
	void deleteStore(
		UserPassport ownerPassport,
		Long queryStoreId
	);
}
