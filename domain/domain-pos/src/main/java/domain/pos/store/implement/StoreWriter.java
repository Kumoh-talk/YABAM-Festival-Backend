package domain.pos.store.implement;

import org.springframework.stereotype.Component;

import com.vo.UserPassport;

import domain.pos.store.entity.Store;
import domain.pos.store.entity.StoreInfo;
import domain.pos.store.port.required.StoreRepository;
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

	public void postDetailImage(Store previousStore, String imageUrl) {
		storeRepository.postDetailImage(previousStore, imageUrl);
	}

	public void deleteDetailImage(Store previousStore, String imageUrl) {
		storeRepository.deleteDetailImage(previousStore, imageUrl);
	}
}
