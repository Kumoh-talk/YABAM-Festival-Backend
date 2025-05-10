package domain.pos.store.implement;

import java.util.Optional;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import domain.pos.store.entity.Store;
import domain.pos.store.entity.dto.StoreHeadDto;
import domain.pos.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StoreReader {
	private final StoreRepository storeRepository;

	public Optional<Store> readSingleStore(Long storeId) {
		return storeRepository.findStoreByStoreId(storeId);
	}

	public Slice<StoreHeadDto> readStores(Long cursorReviewCount, Long cursorStoreId, int size) {
		return storeRepository.findStoresCursorOrderByReviewCount(cursorReviewCount, cursorStoreId, size);
	}
}
