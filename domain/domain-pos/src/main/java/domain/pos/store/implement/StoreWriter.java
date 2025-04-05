package domain.pos.store.implement;

import org.springframework.stereotype.Component;

import domain.pos.member.entity.UserPassport;
import domain.pos.store.entity.Store;
import domain.pos.store.entity.StoreInfo;
import domain.pos.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StoreWriter {
	private final StoreRepository storeRepository;

	public Long createStore(UserPassport userPassport, StoreInfo createRequestStoreInfo) {
		return storeRepository.createStore(userPassport, createRequestStoreInfo);
	}

	public Store updateStoreInfo(Store previousStore, StoreInfo requestChangeStoreInfo) {
		return storeRepository.changeStoreInfo(previousStore, requestChangeStoreInfo);
	}

	public void deleteStore(Store previousStore) {
		storeRepository.deleteStore(previousStore);
	}

	public Store modifyStoreOpenStatus(Store previousStore) {
		return storeRepository.changeStoreOpenStatus(previousStore);
	}
}
