package domain.pos.store.adapter.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vo.UserPassport;

import domain.pos.store.entity.Store;
import domain.pos.store.entity.StoreInfo;
import domain.pos.store.implement.StoreValidator;
import domain.pos.store.port.provided.StoreCommand;
import domain.pos.store.port.required.DetailImageRepository;
import domain.pos.store.port.required.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreCommandImpl implements StoreCommand {
	private final StoreValidator storeValidator;
	private final StoreRepository storeRepository;
	private final DetailImageRepository detailImageRepository;

	@Override
	public Store createStore(UserPassport ownerPassport, StoreInfo createRequestStoreInfo) {
		var store = Store.create(ownerPassport, createRequestStoreInfo);

		return storeRepository.saveStore(store);
	}

	@Transactional
	@Override
	public Store updateStoreInfo(
		final UserPassport ownerPassport,
		final Long queryStoreId,
		final StoreInfo modifyStoreInfo) {
		var savedStore = storeValidator.validateStoreOwner(ownerPassport, queryStoreId);

		savedStore.update(ownerPassport, modifyStoreInfo);

		return storeRepository.updateStore(savedStore);
	}

	@Transactional
	@Override
	public void deleteStore(
		final UserPassport ownerPassport,
		final Long queryStoreId
	) {
		Store store = storeValidator.validateStoreOwner(ownerPassport, queryStoreId);

		storeRepository.deleteStore(store);
	}

	@Transactional
	@Override
	public void registerStoreDetailImage(
		final UserPassport ownerPassport,
		final Long queryStoreId,
		final String imageUrl
	) {
		storeValidator.existsStoreOwner(ownerPassport, queryStoreId);

		var detailImages = detailImageRepository.findByStoreId(queryStoreId);

		detailImages.register(imageUrl);

		detailImageRepository.save(detailImages);
	}

	@Transactional
	@Override
	public void deleteStoreDetailImage(
		final UserPassport ownerPassport,
		final Long queryStoreId,
		final String imageUrl
	) {
		storeValidator.existsStoreOwner(ownerPassport, queryStoreId);

		var detailImages = detailImageRepository.findByStoreId(queryStoreId);

		detailImages.remove(imageUrl);

		detailImageRepository.save(detailImages);
	}

}
