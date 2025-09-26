package com.pos.sale.repository;

import static com.pos.fixtures.sale.SaleFixture.*;
import static com.pos.fixtures.store.StoreEntityFixture.*;
import static fixtures.store.StoreFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;

import com.pos.global.config.RepositoryTest;
import com.pos.sale.entity.SaleEntity;
import com.pos.sale.mapper.SaleMapper;
import com.pos.store.entity.StoreEntity;
import com.pos.store.mapper.StoreMapper;

import domain.pos.sale.entity.Sale;
import domain.pos.sale.port.required.SaleRepository;
import domain.pos.store.entity.Store;

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
		SaleEntity saleEntity = testFixtureBuilder.buildSaleEntity(GENERAL_SALE(savedStoreEntity));
		testEntityManager.flush();
		testEntityManager.clear();

		// when
		System.out.println("===SaleRepositoryImplTest.Sale와Store_DB데이터를_찾을수_있다 쿼리===");
		Sale resultSale = saleRepository.findSaleWithStoreBySaleId(saleEntity.getId()).get();

		// then
		assertSoftly(softly -> {
			softly.assertThat(resultSale.getId()).isEqualTo(saleEntity.getId());
			softly.assertThat(resultSale.getStore().getId()).isEqualTo(savedStore.getId());
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
		Sale sale = saleRepository.findSaleWithStoreBySaleId(resultSale.getId()).get();
		assertSoftly(softly -> {
			softly.assertThat(sale.getId()).isEqualTo(resultSale.getId());
			softly.assertThat(sale.getStore().getId()).isEqualTo(savedStore.getId());
			softly.assertThat(sale.getStore().getOwnerPassport().getUserId())
				.isEqualTo(savedStore.getOwnerPassport().getUserId());
			softly.assertThat(sale.getOpenDateTime()).isNotNull();
			softly.assertThat(sale.getCloseDateTime()).isEqualTo(Optional.empty());
		});
	}

	@Test
	void Sale_마감_테스트() {
		// given
		SaleEntity saleEntity = testFixtureBuilder.buildSaleEntity(GENERAL_SALE(savedStoreEntity));
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
		Sale sale = saleRepository.findSaleWithStoreBySaleId(resultSale.getId()).get();
		assertSoftly(softly -> {
			softly.assertThat(sale.getId()).isEqualTo(savedSale.getId());
			softly.assertThat(sale.getStore().getId()).isEqualTo(savedStore.getId());
			softly.assertThat(sale.getStore().getOwnerPassport().getUserId())
				.isEqualTo(savedStore.getOwnerPassport().getUserId());
			softly.assertThat(sale.getOpenDateTime()).isNotNull();
			softly.assertThat(sale.getCloseDateTime()).isNotEmpty();
		});
	}

	@Test
	void SaleCursor_리스트_조회_테스트() {
		// given
		SaleEntity saleEntity = testFixtureBuilder.buildSaleEntity(GENERAL_SALE(savedStoreEntity));
		SaleEntity saleEntity2 = testFixtureBuilder.buildSaleEntity(GENERAL_SALE(savedStoreEntity));
		SaleEntity saleEntity3 = testFixtureBuilder.buildSaleEntity(GENERAL_SALE(savedStoreEntity));
		testEntityManager.flush();
		testEntityManager.clear();

		// when
		System.out.println("===SaleRepositoryImplTest.SaleCursor_리스트_조회 쿼리===");
		Slice<Sale> resultSale = saleRepository.getSaleSliceByStoreId(savedStore.getId(), null, 3);
		System.out.println("===SaleRepositoryImplTest.SaleCursor_리스트_조회 쿼리===");

		// then
		assertSoftly(softly -> {
			softly.assertThat(resultSale.getContent().size()).isEqualTo(3);
			softly.assertThat(resultSale.hasNext()).isFalse();
			softly.assertThat(resultSale.getContent().get(0).getId()).isEqualTo(saleEntity3.getId());
			softly.assertThat(resultSale.getContent().get(1).getId()).isEqualTo(saleEntity2.getId());
			softly.assertThat(resultSale.getContent().get(2).getId()).isEqualTo(saleEntity.getId());
		});
	}

	@Test
	void findOpenSaleByStoreIdTest() {
		// given
		var openSaleEntity = testFixtureBuilder.buildSaleEntity(GENERAL_SALE(savedStoreEntity));
		testEntityManager.flush();
		testEntityManager.clear();

		// when
		System.out.println("===SaleRepositoryImplTest.SaleCursor_리스트_조회 쿼리===");
		Optional<Sale> result = saleRepository.findOpenSaleByStoreId(savedStore.getId());
		System.out.println("===SaleRepositoryImplTest.SaleCursor_리스트_조회 쿼리===");

		assertThat(result).isNotEmpty();
		assertThat(result.get().getId()).isEqualTo(openSaleEntity.getId());
	}

	@Test
	void findOpenSaleByStoreIdFailCloseTest() {
		// given
		testFixtureBuilder.buildSaleEntity(CLOSED_SALE(savedStoreEntity));
		testEntityManager.flush();
		testEntityManager.clear();

		// when
		System.out.println("===SaleRepositoryImplTest.SaleCursor_리스트_조회 쿼리===");
		Optional<Sale> result = saleRepository.findOpenSaleByStoreId(savedStore.getId());
		System.out.println("===SaleRepositoryImplTest.SaleCursor_리스트_조회 쿼리===");

		assertThat(result).isEmpty();
	}

}
