package com.pos.call.repository;

import static com.pos.fixtures.sale.SaleFixture.*;
import static com.pos.fixtures.store.StoreEntityFixture.*;
import static com.pos.fixtures.table.TableEntityFixture.*;
import static com.pos.receipt.ReceiptEntityFixture.*;
import static fixtures.call.CallFixture.*;
import static fixtures.member.UserFixture.*;
import static fixtures.store.StoreFixture.*;
import static org.assertj.core.api.SoftAssertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.pos.call.entity.CallEntity;
import com.pos.global.config.RepositoryTest;
import com.pos.receipt.entity.ReceiptEntity;
import com.pos.receipt.mapper.ReceiptMapper;
import com.pos.sale.entity.SaleEntity;
import com.pos.sale.mapper.SaleMapper;
import com.pos.store.entity.StoreEntity;
import com.pos.store.mapper.StoreMapper;
import com.pos.table.entity.TableEntity;
import com.pos.table.mapper.TableMapper;
import com.vo.UserPassport;

import domain.pos.call.entity.Call;
import domain.pos.call.entity.CallMessage;
import domain.pos.call.repository.CallRepository;
import domain.pos.receipt.entity.Receipt;
import domain.pos.store.entity.Sale;
import domain.pos.store.entity.Store;

class CallRepositoryImplTest extends RepositoryTest {

	@Autowired
	private CallRepository callRepository;

	private StoreEntity savedStoreEntity;
	private TableEntity savedTableEntity;
	private SaleEntity savedSaleEntity;
	private ReceiptEntity savedReceiptEntity;

	private Store savedStore;
	private Sale savedSale;
	private Receipt savedReceipt;

	@BeforeEach
	void setUp() {
		savedStoreEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(GENERAL_OPEN_STORE()));
		savedTableEntity = testFixtureBuilder.buildTableEntityList(TABLEENTITY_LIST(1, savedStoreEntity)).get(0);
		savedSaleEntity = testFixtureBuilder.buildSaleEntity(GENERAL_SALE(savedStoreEntity));
		savedReceiptEntity = testFixtureBuilder.buildReceiptEntity(
			GENERAL_ADJUSTMENT_RECEIPT(savedSaleEntity, savedTableEntity));

		savedStore = StoreMapper.toStore(savedStoreEntity);
		savedSale = SaleMapper.toSale(savedSaleEntity, savedStore);
		savedReceipt = ReceiptMapper.toReceipt(
			savedReceiptEntity,
			TableMapper.toTable(savedTableEntity, savedStore),
			savedSale);

		testEntityManager.flush();
		testEntityManager.clear();
	}

	@Test
	void 호출_저장_및_조회() {
		// given
		UUID receiptId = savedReceiptEntity.getId();
		Long saleId = savedSaleEntity.getId();
		CallMessage callMessage = GENERAL_CALL_MESSAGE();

		// when
		System.out.println("===CallRepositoryImplTest.호출_저장_및_조회 쿼리===");
		callRepository.createCall(receiptId, saleId, callMessage);
		testEntityManager.flush();
		testEntityManager.clear();
		System.out.println("===CallRepositoryImplTest.호출_저장_및_조회 쿼리===");

		// then
		assertSoftly(softly -> {
			var slice = callRepository.getNonCompleteCalls(saleId, null, 10);
			softly.assertThat(slice.getContent()).hasSize(1);
			Call call = slice.getContent().get(0);
			softly.assertThat(call.getCallMessage().getMessage()).isEqualTo(callMessage.getMessage());
			softly.assertThat(call.getCallMessage().getIsComplete()).isFalse();
		});
	}

	/*----------------------------------------------------------------------
	 * getNonCompleteCalls (Cursor/Paging)
	 *--------------------------------------------------------------------*/
	@Test
	void 미완료_호출_슬라이스_조회() {
		// given : 호출 3건 저장
		UUID receiptId = savedReceiptEntity.getId();
		Long saleId = savedSaleEntity.getId();

		for (int i = 0; i < 3; i++) {
			callRepository.createCall(receiptId, saleId, GENERAL_CALL_MESSAGE());
		}
		testEntityManager.flush();
		testEntityManager.clear();

		// when
		System.out.println("===CallRepositoryImplTest.미완료_호출_슬라이스_조회 쿼리===");
		var slice = callRepository.getNonCompleteCalls(saleId, null, 2); // pageSize = 2
		System.out.println("===CallRepositoryImplTest.미완료_호출_슬라이스_조회 쿼리===");

		// then
		assertSoftly(softly -> {
			softly.assertThat(slice.getContent()).hasSize(2);
			softly.assertThat(slice.hasNext()).isTrue();
		});
	}

	@Test
	void 호출_완료_처리() {
		// given
		UUID receiptId = savedReceiptEntity.getId();
		Long saleId = savedSaleEntity.getId();
		callRepository.createCall(receiptId, saleId, GENERAL_CALL_MESSAGE());
		testEntityManager.flush();
		testEntityManager.clear();

		CallEntity callEntity = testEntityManager.getEntityManager()
			.createQuery("select c from CallEntity c", CallEntity.class)
			.getSingleResult();
		Long callId = callEntity.getId();

		// when
		System.out.println("===CallRepositoryImplTest.호출_완료_처리 쿼리===");
		callRepository.modifyCallComplete(callId);
		testEntityManager.flush();
		testEntityManager.clear();
		System.out.println("===CallRepositoryImplTest.호출_완료_처리 쿼리===");

		// then
		assertSoftly(softly -> {
			CallEntity updated = testEntityManager.find(CallEntity.class, callId);
			softly.assertThat(updated.getIsCompleted()).isTrue();

			var slice = callRepository.getNonCompleteCalls(saleId, null, 10);
			softly.assertThat(slice.getContent()).isEmpty();
		});
	}

	@Test
	void 호출_완료_처리_실패_CALL_NOT_FOUND() {
		// given
		Long invalidCallId = 999L;

		// when -> then
		assertSoftly(softly -> {
			softly.assertThatThrownBy(() -> callRepository.modifyCallComplete(invalidCallId))
				.isInstanceOf(ServiceException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_CALL);
		});
	}

	@Nested
	@DisplayName("isExistsCallOwner 테스트")
	class ExistsCallOwner {

		private Long callId;

		@BeforeEach
		void createCall() {
			UUID receiptId = savedReceiptEntity.getId();
			Long saleId = savedSaleEntity.getId();
			callRepository.createCall(receiptId, saleId, GENERAL_CALL_MESSAGE());
			testEntityManager.flush();
			testEntityManager.clear();

			callId = testEntityManager.getEntityManager()
				.createQuery("select c from CallEntity c", CallEntity.class)
				.getSingleResult().getId();
		}

		@Test
		void 성공_TRUE() {
			// given
			UserPassport ownerPassport = OWNER_USER_PASSPORT();

			// when
			System.out.println("===CallRepositoryImplTest.성공_TRUE 쿼리===");
			boolean exists = callRepository.isExistsCallOwner(callId, ownerPassport);
			System.out.println("===CallRepositoryImplTest.성공_TRUE 쿼리===");
			// then
			assertSoftly(softly -> softly.assertThat(exists).isTrue());
		}

		@Test
		void 실패_FALSE() {
			// given
			UserPassport diffOwnerPassport = DIFF_OWNER_PASSPORT();

			// when
			System.out.println("===CallRepositoryImplTest.실패_FALSE 쿼리===");
			boolean exists = callRepository.isExistsCallOwner(callId, diffOwnerPassport);
			System.out.println("===CallRepositoryImplTest.실패_FALSE 쿼리===");
			// then
			assertSoftly(softly -> softly.assertThat(exists).isFalse());
		}
	}
}
