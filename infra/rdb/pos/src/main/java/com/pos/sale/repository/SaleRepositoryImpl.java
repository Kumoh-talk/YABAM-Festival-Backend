package com.pos.sale.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.pos.sale.entity.QSaleEntity;
import com.pos.sale.entity.SaleEntity;
import com.pos.sale.mapper.SaleMapper;
import com.pos.store.entity.QStoreEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import domain.pos.store.entity.Sale;
import domain.pos.store.entity.Store;
import domain.pos.store.repository.SaleRepository;
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
	public Sale closeSale(Sale savedSale, Store closeStore) {
		LocalDateTime now = LocalDateTime.now();
		queryFactory
			.update(qSaleEntity)
			.where(qSaleEntity.id.eq(savedSale.getSaleId()))
			.set(qSaleEntity.closeDateTime, now)
			.execute();
		return SaleMapper.toClosedSale(savedSale, closeStore, now);
	}
}
