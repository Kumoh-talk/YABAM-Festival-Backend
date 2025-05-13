package com.pos.store.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.pos.store.entity.QStoreDetailImageEntity;
import com.pos.store.entity.QStoreEntity;
import com.pos.store.entity.StoreDetailImageEntity;
import com.pos.store.entity.StoreEntity;
import com.pos.store.mapper.StoreMapper;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vo.UserPassport;

import domain.pos.store.entity.Store;
import domain.pos.store.entity.StoreInfo;
import domain.pos.store.entity.dto.StoreHeadDto;
import domain.pos.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepository {
	private final StoreJpaRepository storeJpaRepository;
	private final StoreDetailImageJpaRepository storeDetailImageJpaRepository;
	private final JPAQueryFactory queryFactory;

	private final QStoreEntity qStoreEntity = QStoreEntity.storeEntity;
	private final QStoreDetailImageEntity qStoreDetailImageEntity = QStoreDetailImageEntity.storeDetailImageEntity;

	private static final boolean IS_CLOSED = false;

	@Override
	public Long createStore(UserPassport userPassport, StoreInfo createRequestStoreInfo) {
		StoreEntity storeEntity = StoreMapper.toStoreEntity(userPassport, createRequestStoreInfo, IS_CLOSED);
		return storeJpaRepository.save(storeEntity).getId();
	}

	@Override
	public Optional<Store> findStoreByStoreId(Long storeId) {
		List<StoreDetailImageEntity> storeDetailImageEntities = queryFactory
			.select(qStoreDetailImageEntity)
			.from(qStoreDetailImageEntity)
			.join(qStoreDetailImageEntity.store, qStoreEntity).fetchJoin()
			.where(qStoreEntity.id.eq(storeId))
			.fetch();

		return StoreMapper.toStore(storeDetailImageEntities);
	}

	@Override
	@Transactional
	public Store changeStoreInfo(Store previousStore, StoreInfo requestChangeStoreInfo) {
		StoreEntity storeEntity = storeJpaRepository.findById(previousStore.getStoreId()).get();
		storeEntity.changeStoreInfo(requestChangeStoreInfo);
		return StoreMapper.toStore(storeEntity);
	}

	@Override
	@Transactional
	public void deleteStore(Store previousStore) {
		queryFactory.update(qStoreEntity)
			.where(qStoreEntity.id.eq(previousStore.getStoreId()))
			.set(qStoreEntity.deletedAt, LocalDateTime.now())
			.execute();
	}

	@Override
	@Transactional
	public Store changeStoreOpenStatus(Store previousStore) {
		queryFactory.update(qStoreEntity)
			.where(qStoreEntity.id.eq(previousStore.getStoreId()))
			.set(qStoreEntity.isOpen, previousStore.getIsOpen())
			.execute();
		return previousStore.open();
	}

	@Override
	public boolean isExistsById(Long storeId) {
		return queryFactory
			.selectOne()
			.from(qStoreEntity)
			.where(qStoreEntity.id.eq(storeId))
			.fetchFirst() != null;
	}

	@Override
	public void postDetailImage(Store previousStore, String imageUrl) {
		storeDetailImageJpaRepository.save(
			StoreDetailImageEntity.of(
				imageUrl,
				StoreEntity.from(previousStore.getStoreId())
			)
		);
	}

	@Override
	public boolean isExistsImageUrl(Long storeId, String imageUrl) {
		return queryFactory
			.selectOne()
			.from(qStoreDetailImageEntity)
			.where(qStoreDetailImageEntity.imageUrl.eq(imageUrl)
				.and(qStoreDetailImageEntity.store.id.eq(storeId)))
			.fetchFirst() != null;
	}

	@Override
	@Transactional
	public void deleteDetailImage(Store previousStore, String imageUrl) {
		queryFactory
			.delete(qStoreDetailImageEntity)
			.where(qStoreDetailImageEntity.imageUrl.eq(imageUrl)
				.and(qStoreDetailImageEntity.store.id.eq(previousStore.getStoreId())))
			.execute();
	}

	@Override
	public Slice<StoreHeadDto> findStoresCursorOrderByReviewCount(Long cursorReviewCount, Long cursorStoreId,
		int size) {
		return storeJpaRepository.findStoreHeadsByReviewCountCursor(cursorReviewCount, cursorStoreId, size);
	}

	@Override
	public List<Store> findMyStores(Long userId) {
		return null;
	}
}
