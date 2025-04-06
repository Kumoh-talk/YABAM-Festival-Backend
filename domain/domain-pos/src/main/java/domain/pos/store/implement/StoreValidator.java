package domain.pos.store.implement;

import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserPassport;
import domain.pos.store.entity.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class StoreValidator {
	private final StoreReader storeReader;

	public Store validateStoreOwner(UserPassport ownerPassport, Long queryStoreId) {
		final Store previousStore = storeReader.readSingleStore(queryStoreId)
			.orElseThrow(() -> {
				log.warn("해당 Store 존재하지 않음: storeId={}", queryStoreId);
				throw new ServiceException(ErrorCode.NOT_FOUND_STORE);
			});

		if (isEqualSavedStoreOwnerAndQueryOwner(ownerPassport.getUserId(), previousStore)) {
			log.warn("요청 유저는 Store 소유자와 다름: userId={}, queryStoreId={}", ownerPassport.getUserId(), queryStoreId);
			throw new ServiceException(ErrorCode.NOT_EQUAL_STORE_OWNER);
		}
		return previousStore;
	}

	// 가게 소유자와 요청 점주가 같은지 확인
	private boolean isEqualSavedStoreOwnerAndQueryOwner(Long ownerId, Store previousStore) {
		return !previousStore.getOwnerPassport().getUserId().equals(ownerId);
	}
}
