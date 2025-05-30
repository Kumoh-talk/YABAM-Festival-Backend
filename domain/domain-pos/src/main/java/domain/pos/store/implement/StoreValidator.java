package domain.pos.store.implement;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import domain.pos.store.entity.Store;
import domain.pos.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class StoreValidator {
	private final StoreReader storeReader;
	private final StoreRepository storeRepository;

	public Store validateStoreOwner(UserPassport ownerPassport, Long queryStoreId) {
		return validateStoreAndOwner(ownerPassport, storeReader.readSingleStore(queryStoreId), queryStoreId);
	}

	private Store validateStoreAndOwner(UserPassport ownerPassport, Optional<Store> optionalStore, Long queryStoreId) {
		Store store = optionalStore
			.orElseThrow(() -> {
				log.warn("해당 Store 존재하지 않음: storeId={}", queryStoreId);
				throw new ServiceException(ErrorCode.NOT_FOUND_STORE);
			});

		if (!isEqualSavedStoreOwnerAndQueryOwner(ownerPassport.getUserId(), store)) {
			log.warn("요청 유저는 Store 소유자와 다름: userId={}, queryStoreId={}", ownerPassport.getUserId(), queryStoreId);
			throw new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER);
		}
		return store;
	}

	public Store validateStoreOwnerWithLock(UserPassport ownerPassport, Long queryStoreId) {
		return validateStoreAndOwner(ownerPassport, storeReader.readSingleStoreWithLock(queryStoreId), queryStoreId);
	}

	public void validateStoreOwner(UserPassport ownerPassport, Store store) {
		if (!isEqualSavedStoreOwnerAndQueryOwner(ownerPassport.getUserId(), store)) {
			log.warn("요청 유저는 Store 소유자와 다름: userId={}, queryStoreId={}", ownerPassport.getUserId(),
				store.getStoreId());
			throw new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER);
		}
	}

	// 가게 소유자와 요청 점주가 같은지 확인
	private boolean isEqualSavedStoreOwnerAndQueryOwner(Long ownerId, Store previousStore) {
		return previousStore.getOwnerPassport().getUserId().equals(ownerId);
	}

	public void validateStore(Long storeId) {
		if (!storeRepository.isExistsById(storeId)) {
			log.warn("해당 Store 존재하지 않음: storeId={}", storeId);
			throw new ServiceException(ErrorCode.NOT_FOUND_STORE);
		}
	}

	public void validateExistDetailImage(Store previousStore, String imageUrl) {
		if (!storeRepository.isExistsImageUrl(previousStore.getStoreId(), imageUrl)) {
			log.warn("해당 Store에 존재하지 않는 이미지 URL: storeId={}, imageUrl={}", previousStore.getStoreId(), imageUrl);
			throw new ServiceException(ErrorCode.NOT_FOUND_STORE_IMAGE);
		}
	}
}
