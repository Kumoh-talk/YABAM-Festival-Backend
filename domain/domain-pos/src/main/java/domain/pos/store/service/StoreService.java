package domain.pos.store.service;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserPassport;
import domain.pos.store.entity.Store;
import domain.pos.store.entity.StoreInfo;
import domain.pos.store.entity.dto.StoreHeadDto;
import domain.pos.store.implement.StoreReader;
import domain.pos.store.implement.StoreValidator;
import domain.pos.store.implement.StoreWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreService {
	private final StoreWriter storeWriter;
	private final StoreReader storeReader;
	private final StoreValidator storeValidator;

	public Long createStore(final UserPassport ownerPassport, final StoreInfo createRequestStoreInfo) {
		final Long savedStoreId = storeWriter.createStore(ownerPassport, createRequestStoreInfo);
		log.info("가게 생성 성공 : userId={}, storeId={}", ownerPassport.getUserId(), savedStoreId);
		return savedStoreId;
	}

	public Store findStore(final Long storeId) {
		log.info("가게 조회: storeId={}", storeId);
		return storeReader.readSingleStore(storeId)
			.orElseThrow(() -> {
				log.warn("가게 조회 실패: storeId={}", storeId);
				throw new ServiceException(ErrorCode.NOT_FOUND_STORE);
			});
	}

	public Slice<StoreHeadDto> findStores(final Long cursorReviewCount, final Long cursorId, final int size) {
		log.info("가게 목록 조회: cursorReviewCount={}, size={}", cursorReviewCount, size);
		return storeReader.readStores(cursorReviewCount, cursorId, size);
	}

	public Store updateStoreInfo(
		final UserPassport ownerPassport,
		final Long queryStoreId,
		final StoreInfo requestChangeStoreInfo) {

		final Store previousStore = storeValidator.validateStoreOwner(ownerPassport, queryStoreId);
		Store updatedStore = storeWriter
			.updateStoreInfo(previousStore, requestChangeStoreInfo);
		log.info("가게 정보 수정 성공 : userId={}, storeId={}", ownerPassport.getUserId(), queryStoreId);

		return updatedStore;
	}

	public void deleteStore(final UserPassport ownerPassport, final Long storeId) {
		final Store previousStore = storeValidator.validateStoreOwner(ownerPassport, storeId);
		storeWriter.deleteStore(previousStore);
		log.info("가게 삭제 성공 : userId={}, storeId={}", ownerPassport.getUserId(), storeId);
	}

	public void postDetailImage(final UserPassport ownerPassport, final Long storeId, final String imageUrl) {
		final Store previousStore = storeValidator.validateStoreOwner(ownerPassport, storeId);
		storeWriter.postDetailImage(previousStore, imageUrl);
		log.info("가게 상세 이미지 등록 성공 : userId={}, storeId={}", ownerPassport.getUserId(), storeId);
	}

	public void deleteDetailImage(final UserPassport ownerPassport, final Long storeId, final String imageUrl) {
		final Store previousStore = storeValidator.validateStoreOwner(ownerPassport, storeId);
		storeValidator.validateExistDetailImage(previousStore, imageUrl);
		storeWriter.deleteDetailImage(previousStore, imageUrl);
		log.info("가게 상세 이미지 삭제 성공 : userId={}, storeId={}", ownerPassport.getUserId(), storeId);
	}

}
