package com.pos.receipt.repository;

import static com.pos.fixtures.sale.SaleFixture.*;
import static com.pos.fixtures.store.StoreEntityFixture.*;
import static com.pos.fixtures.table.TableEntityFixture.*;
import static fixtures.store.StoreFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.pos.global.config.RepositoryTest;
import com.pos.receipt.ReceiptEntityFixture;
import com.pos.receipt.entity.ReceiptEntity;
import com.pos.sale.entity.SaleEntity;
import com.pos.sale.mapper.SaleMapper;
import com.pos.store.entity.StoreEntity;
import com.pos.store.mapper.StoreMapper;
import com.pos.table.entity.TableEntity;
import com.pos.table.mapper.TableMapper;
import com.pos.table.vo.TableNumber;
import com.pos.table.vo.TablePointVo;

import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.receipt.repository.ReceiptRepository;
import domain.pos.sale.entity.Sale;
import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;

class ReceiptRepositoryImplTest extends RepositoryTest {

	@Autowired
	private ReceiptRepository receiptRepository;

	private StoreEntity savedStoreEntity;
	private SaleEntity savedSaleEntity;
	private TableEntity savedTableEntity;
	private Sale savedSale;
	private Table savedTable;

	@BeforeEach
	void setUp() {
		savedStoreEntity = testFixtureBuilder.buildStoreEntity(CUSTOM_STORE_ENTITY(GENERAL_CLOSE_STORE()));
		savedSaleEntity = testFixtureBuilder.buildSaleEntity(GENERAL_SALE(savedStoreEntity));
		savedTableEntity = testFixtureBuilder.buildTableEntity(GENERAL_TABLE_ENTITY(savedStoreEntity));
		testEntityManager.flush();
		testEntityManager.clear();

		Store savedStore = StoreMapper.toStore(savedStoreEntity);
		savedSale = SaleMapper.toSale(savedSaleEntity, savedStore);
		savedTable = TableMapper.toTable(savedTableEntity, (Store)null);
	}

	@Test
	void createReceipt_정상_생성() {
		Receipt receipt = receiptRepository.createReceipt(savedTable, savedSale);
		testEntityManager.flush();
		testEntityManager.clear();

		assertSoftly(softly -> {
			softly.assertThat(receipt.getReceiptInfo().getReceiptId()).isNotNull();
			softly.assertThat(receipt.getReceiptInfo().isAdjustment()).isFalse();
			softly.assertThat(receipt.getReceiptInfo().getStartUsageTime()).isNull();
		});
	}

	@Test
	void getReceiptInfo_ID로_조회() {
		ReceiptEntity savedReceipt = testFixtureBuilder.buildReceiptEntity(
			ReceiptEntityFixture.GENERAL_ADJUSTMENT_RECEIPT(savedSaleEntity, savedTableEntity)
		);
		testEntityManager.flush();
		testEntityManager.clear();

		Optional<ReceiptInfo> result = receiptRepository.getReceiptInfo(savedReceipt.getId());

		assertSoftly(softly -> {
			softly.assertThat(result).isPresent();
			softly.assertThat(result.get().getReceiptId()).isEqualTo(savedReceipt.getId());
			softly.assertThat(result.get().isAdjustment()).isTrue();
		});
	}

	@Test
	void getReceiptInfo_없는_ID는_빈_Optional_반환() {
		Optional<ReceiptInfo> result = receiptRepository.getReceiptInfo(UUID.randomUUID());

		assertThat(result).isEmpty();
	}

	@Test
	void existsReceipt_존재하는_영수증_true_반환() {
		ReceiptEntity savedReceipt = testFixtureBuilder.buildReceiptEntity(
			ReceiptEntity.of(savedSaleEntity, savedTableEntity)
		);
		testEntityManager.flush();
		testEntityManager.clear();

		assertThat(receiptRepository.existsReceipt(savedReceipt.getId())).isTrue();
	}

	@Test
	void existsReceipt_없는_영수증_false_반환() {
		assertThat(receiptRepository.existsReceipt(UUID.randomUUID())).isFalse();
	}

	@Test
	void getAllNonAdjustReceiptWithTableAndOrders_테이블_번호_오름차순_정렬() {
		TableEntity tableNumber1 = testFixtureBuilder.buildTableEntity(
			TableEntity.of(TableNumber.from(1), TablePointVo.of(0, 0), false, 4, savedStoreEntity));
		TableEntity tableNumber3 = testFixtureBuilder.buildTableEntity(
			TableEntity.of(TableNumber.from(3), TablePointVo.of(0, 0), false, 4, savedStoreEntity));
		TableEntity tableNumber2 = testFixtureBuilder.buildTableEntity(
			TableEntity.of(TableNumber.from(2), TablePointVo.of(0, 0), false, 4, savedStoreEntity));

		testFixtureBuilder.buildReceiptEntity(ReceiptEntity.of(savedSaleEntity, tableNumber3));
		testFixtureBuilder.buildReceiptEntity(ReceiptEntity.of(savedSaleEntity, tableNumber1));
		testFixtureBuilder.buildReceiptEntity(ReceiptEntity.of(savedSaleEntity, tableNumber2));
		testEntityManager.flush();
		testEntityManager.clear();

		List<Receipt> receipts = receiptRepository.getAllNonAdjustReceiptWithTableAndOrders(savedSaleEntity.getId());

		assertThat(receipts).hasSize(3);
		assertThat(receipts.get(0).getTable().getTableNumber().value()).isEqualTo(1);
		assertThat(receipts.get(1).getTable().getTableNumber().value()).isEqualTo(2);
		assertThat(receipts.get(2).getTable().getTableNumber().value()).isEqualTo(3);
	}
}
