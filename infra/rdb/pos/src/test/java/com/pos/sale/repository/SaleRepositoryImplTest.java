package com.pos.sale.repository;

import static com.pos.fixtures.store.StoreEntityFixture.*;
import static fixtures.store.StoreFixture.*;
import static org.assertj.core.api.SoftAssertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.pos.fixtures.sale.SaleFixture;
import com.pos.global.config.RepositoryTest;
import com.pos.sale.entity.SaleEntity;
import com.pos.sale.mapper.SaleMapper;
import com.pos.store.entity.StoreEntity;
import com.pos.store.mapper.StoreMapper;

import domain.pos.store.entity.Sale;
import domain.pos.store.entity.Store;
import domain.pos.store.repository.SaleRepository;

class SaleRepositoryImplTest extends RepositoryTest {

	@Autowired
	private SaleRepository saleRepository;

	private StoreEntity savedStoreEntity;
	private Store savedStore;

	@BeforeEach
	void setUp() {
		savedStoreEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(GENERAL_CLOSE_STORE()));
		savedStore = StoreMapper.toStore(savedStoreEntity);
		testEntityManager.flush();
		testEntityManager.clear();
	}

	@Test
	void Sale와Store_fetchjoin_테스트() {
		// given
		SaleEntity saleEntity = testFixtureBuilder.buildSaleEntity(SaleFixture.GENERAL_SALE(savedStoreEntity));
		testEntityManager.flush();
		testEntityManager.clear();

		// when
		System.out.println("===SaleRepositoryImplTest.Sale와Store_DB데이터를_찾을수_있다 쿼리===");
		Sale resultSale = saleRepository.findSaleWithStoreBySaleId(saleEntity.getId()).get();

		// then
		assertSoftly(softly -> {
			softly.assertThat(resultSale.getSaleId()).isEqualTo(saleEntity.getId());
			softly.assertThat(resultSale.getStore().getStoreId()).isEqualTo(savedStore.getStoreId());
			softly.assertThat(resultSale.getStore().getOwnerPassport().getUserId())
				.isEqualTo(savedStore.getOwnerPassport().getUserId());
		});
	}

	@Test
	void Sale생성_테스트() {
		// when
		System.out.println("===SaleRepositoryImplTest.Sale생성 쿼리===");
		Sale resultSale = saleRepository.createSale(savedStore);
		testEntityManager.flush();
		testEntityManager.clear();
		System.out.println("===SaleRepositoryImplTest.Sale생성 쿼리===");
		// then
		Sale sale = saleRepository.findSaleWithStoreBySaleId(resultSale.getSaleId()).get();
		assertSoftly(softly -> {
			softly.assertThat(sale.getSaleId()).isEqualTo(resultSale.getSaleId());
			softly.assertThat(sale.getStore().getStoreId()).isEqualTo(savedStore.getStoreId());
			softly.assertThat(sale.getStore().getOwnerPassport().getUserId())
				.isEqualTo(savedStore.getOwnerPassport().getUserId());
			softly.assertThat(sale.getOpenDateTime()).isNotNull();
			softly.assertThat(sale.getCloseDateTime()).isEqualTo(Optional.empty());
		});
	}

	@Test
	void Sale_마감_테스트() {
		// given
		SaleEntity saleEntity = testFixtureBuilder.buildSaleEntity(SaleFixture.GENERAL_SALE(savedStoreEntity));
		Sale savedSale = SaleMapper.toSale(saleEntity, savedStore);
		testEntityManager.flush();
		testEntityManager.clear();

		// when
		System.out.println("===SaleRepositoryImplTest.Sale_마감 쿼리===");
		Sale resultSale = saleRepository.closeSale(savedSale, savedStore);
		testEntityManager.flush();
		testEntityManager.clear();
		System.out.println("===SaleRepositoryImplTest.Sale_마감 쿼리===");
		// then
		Sale sale = saleRepository.findSaleWithStoreBySaleId(resultSale.getSaleId()).get();
		assertSoftly(softly -> {
			softly.assertThat(sale.getSaleId()).isEqualTo(savedSale.getSaleId());
			softly.assertThat(sale.getStore().getStoreId()).isEqualTo(savedStore.getStoreId());
			softly.assertThat(sale.getStore().getOwnerPassport().getUserId())
				.isEqualTo(savedStore.getOwnerPassport().getUserId());
			softly.assertThat(sale.getOpenDateTime()).isNotNull();
			softly.assertThat(sale.getCloseDateTime()).isNotEmpty();
		});
	}

}
