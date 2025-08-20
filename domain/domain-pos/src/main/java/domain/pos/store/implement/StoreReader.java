package domain.pos.store.implement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import com.vo.UserPassport;

import domain.pos.store.entity.Store;
import domain.pos.store.entity.dto.StoreHeadDto;
import domain.pos.store.port.required.StoreRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StoreReader {
	private final StoreRepository storeRepository;

	public Optional<Store> readSingleStore(Long storeId) {
		return storeRepository.findStoreByStoreId(storeId);
	}

	public Slice<StoreHeadDto> readStores(Long lastStoreId, int size) {
		return storeRepository.findStoresCursorOrderByCreated(lastStoreId, size);
	}

	public List<Store> readMyStores(UserPassport ownerPassport) {
		return storeRepository.findMyStores(ownerPassport.getUserId());
	}

	public Optional<Store> readSingleStoreWithLock(Long queryStoreId) {
		return storeRepository.findStoreByStoreIdWithLock(queryStoreId);
	}
}
