package domain.pos.store.port.provided;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.vo.UserPassport;

import domain.pos.store.entity.Store;
import domain.pos.store.entity.dto.StoreHeadDto;

public interface StoreRead {
	Store findStore(Long storeId);

	Slice<StoreHeadDto> findStores(Long lastStoreId, int size);

	List<Store> getMyStores(UserPassport ownerPassport);
}
