package com.pos.sale.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.pos.sale.entity.QSaleEntity;
import com.pos.sale.entity.SaleEntity;
import com.pos.sale.mapper.SaleMapper;
import com.pos.store.entity.QStoreEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import domain.pos.sale.entity.Sale;
import domain.pos.sale.port.required.SaleRepository;
import domain.pos.store.entity.Store;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SaleRepositoryImpl implements SaleRepository {
	private final SaleJpaRepository saleJpaRepository;
	private final JPAQueryFactory queryFactory;

	private final QSaleEntity qSaleEntity = QSaleEntity.saleEntity;
	private final QStoreEntity qStoreEntity = QStoreEntity.storeEntity;

	@Override
	public Sale createSale(Store previousStore) {
		SaleEntity saleEntity = SaleMapper.toSaleEntity(previousStore);
		SaleEntity saveSaleEntity = saleJpaRepository.save(saleEntity);
		return SaleMapper.toSale(saveSaleEntity, previousStore);
	}

	@Override
	public Optional<Sale> findSaleWithStoreBySaleId(Long saleId) {
		Optional<SaleEntity> saleEntity = Optional.ofNullable(queryFactory
			.selectFrom(qSaleEntity)
			.join(qSaleEntity.store, qStoreEntity).fetchJoin()
			.where(qSaleEntity.id.eq(saleId))
			.fetchOne());
		return saleEntity
			.map(SaleMapper::toSaleWithStore);
	}

	@Override
	public Optional<Sale> getOpenSaleByStoreId(Long storeId) {
		Optional<SaleEntity> saleEntity = Optional.ofNullable(queryFactory
			.selectFrom(qSaleEntity)
			.join(qSaleEntity.store, qStoreEntity).fetchJoin()
			.where(qSaleEntity.store.id.eq(storeId)
				.and(qSaleEntity.closeDateTime.isNull()))
			.fetchOne());
		return saleEntity
			.map(SaleMapper::toSaleWithStore);
	}

	@Override
	public Sale closeSale(Sale savedSale, Store closeStore) {
		LocalDateTime now = LocalDateTime.now();
		queryFactory
			.update(qSaleEntity)
			.where(qSaleEntity.id.eq(savedSale.getId()))
			.set(qSaleEntity.closeDateTime, now)
			.execute();
		return SaleMapper.toClosedSale(savedSale, closeStore, now);
	}

	@Override
	public Slice<Sale> getSaleSliceByStoreId(Long storeId, Long lastSaleId, int size) {
		List<SaleEntity> fetch = queryFactory.select(qSaleEntity)
			.from(qSaleEntity)
			.where(saleSliceCursorCondition(storeId, lastSaleId))
			.orderBy(qSaleEntity.id.desc())
			.limit(size + 1)
			.fetch();
		boolean hasNext = false;
		if (fetch.size() > size) {
			hasNext = true;
			fetch.remove(size);
		}
		List<Sale> list = fetch.stream()
			.map(SaleMapper::toSale)
			.toList();

		return new SliceImpl<>(list, PageRequest.of(0, size), hasNext);
	}

	private BooleanExpression saleSliceCursorCondition(Long storeId, Long lastSaleId) {
		if (lastSaleId == null) {
			return qSaleEntity.store.id.eq(storeId);
		}
		return qSaleEntity.store.id.eq(storeId)
			.and(qSaleEntity.id.lt(lastSaleId));
	}
}
