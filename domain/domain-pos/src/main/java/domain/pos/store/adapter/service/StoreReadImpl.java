package domain.pos.store.adapter.service;

import java.util.List;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;

import domain.pos.store.entity.Store;
import domain.pos.store.entity.dto.StoreHeadDto;
import domain.pos.store.port.provided.StoreRead;
import domain.pos.store.port.required.StoreRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreReadImpl implements StoreRead {
	private final StoreRepository storeRepository;

	@Override
	public Store findStore(final Long storeId) {
		return storeRepository.findStoreByStoreId(storeId)
			.orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND_STORE));
	}

	@Override
	public Slice<StoreHeadDto> findStores(final Long lastStoreId, final int size) {
		return storeRepository.findStoresCursorOrderByCreated(lastStoreId, size);
	}

	@Override
	public List<Store> getMyStores(final UserPassport ownerPassport) {
		return storeRepository.findMyStores(ownerPassport.getUserId());
	}
}
