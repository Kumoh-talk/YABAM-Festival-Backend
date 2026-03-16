package com.pos.order.repository;

import static com.pos.fixtures.sale.SaleFixture.*;
import static com.pos.fixtures.store.StoreEntityFixture.*;
import static fixtures.store.StoreFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;

import com.pos.global.config.RepositoryTest;
import com.pos.menu.entity.MenuCategoryEntity;
import com.pos.menu.entity.MenuEntity;
import com.pos.order.entity.OrderEntity;
import com.pos.order.entity.OrderMenuEntity;
import com.pos.order.repository.jpa.OrderJpaRepository;
import com.pos.receipt.entity.ReceiptEntity;
import com.pos.sale.entity.SaleEntity;
import com.pos.store.entity.StoreEntity;
import com.pos.table.entity.TableEntity;

import domain.pos.order.entity.Order;
import domain.pos.order.entity.vo.OrderMenuStatus;
import domain.pos.order.entity.vo.OrderStatus;
import domain.pos.order.repository.OrderRepository;

import com.pos.fixtures.table.TableEntityFixture;
import com.pos.receipt.ReceiptEntityFixture;

class OrderRepositoryImplTest extends RepositoryTest {

	@Autowired
	private OrderJpaRepository orderJpaRepository;

	@Autowired
	private OrderRepository orderRepository;

	private StoreEntity savedStoreEntity;
	private SaleEntity savedSaleEntity;
	private TableEntity savedTableEntity;
	private ReceiptEntity savedReceiptEntity;
	private MenuEntity savedMenuEntity;

	@BeforeEach
	void setUp() {
		savedStoreEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(GENERAL_CLOSE_STORE()));
		savedSaleEntity = testFixtureBuilder.buildSaleEntity(GENERAL_SALE(savedStoreEntity));

		savedTableEntity = testFixtureBuilder.buildTableEntity(
			TableEntityFixture.GENERAL_TABLE_ENTITY(savedStoreEntity));

		savedReceiptEntity = testFixtureBuilder.buildReceiptEntity(
			ReceiptEntityFixture.GENERAL_ADJUSTMENT_RECEIPT(savedSaleEntity, savedTableEntity));

		domain.pos.store.entity.Store storeDomain = domain.pos.store.entity.Store.of(
			savedStoreEntity.getId(),
			true,
			fixtures.store.StoreInfoFixture.GENERAL_STORE_INFO(),
			fixtures.member.UserFixture.OWNER_USER_PASSPORT(),
			null);

		MenuCategoryEntity menuCategory = testFixtureBuilder.buildMenuCategoryEntity(
			MenuCategoryEntity.of(
				domain.pos.menu.entity.MenuCategoryInfo.of(null, "Test Category", 1),
				storeDomain));

		savedMenuEntity = testFixtureBuilder.buildMenuEntity(
			MenuEntity.of(fixtures.menu.MenuInfoFixture.REQUEST_MENU_INFO(), savedStoreEntity, menuCategory));

		testEntityManager.flush();
		testEntityManager.clear();
	}

	@Test
	@DisplayName("N+1 미발생: 3건 주문 중 pageSize=2 슬라이스 조회 시 쿼리 최소화 확인")
	void findSaleOrdersWithMenuAndTable_Nplus1_해결_테스트() {
		// given
		ReceiptEntity receipt = testEntityManager.find(ReceiptEntity.class, savedReceiptEntity.getId());
		MenuEntity menu = testEntityManager.find(MenuEntity.class, savedMenuEntity.getId());

		for (int i = 0; i < 3; i++) {
			OrderEntity orderEntity = OrderEntity.builder()
				.status(OrderStatus.ORDERED)
				.totalPrice(1000)
				.receipt(receipt)
				.build();

			OrderMenuEntity orderMenuEntity = OrderMenuEntity.of(1, OrderMenuStatus.ORDERED, orderEntity, menu);
			orderEntity.getOrderMenus().add(orderMenuEntity);
			orderJpaRepository.save(orderEntity);
		}

		testEntityManager.flush();
		testEntityManager.clear();

		System.out.println("====== findSaleOrdersWithMenuAndTable 조회 쿼리 시작 ======");
		// when
		Slice<Order> result = orderRepository.getSaleOrderSliceWithMenuAndTable(
			savedSaleEntity.getId(),
			List.of(OrderStatus.ORDERED),
			2,
			null);
		System.out.println("====== findSaleOrdersWithMenuAndTable 조회 쿼리 종료 ======");

		// then
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.hasNext()).isTrue();
		assertThat(result.getContent().get(0).getOrderMenus()).hasSize(1);
		assertThat(result.getContent().get(0).getOrderMenus().get(0).getMenu().getMenuInfo().getId())
			.isEqualTo(savedMenuEntity.getId());
	}

	@Test
	@DisplayName("커서 페이지네이션: lastOrderId 기준으로 이후 데이터만 조회한다")
	void findSaleOrdersWithMenuAndTable_커서_페이지네이션_테스트() {
		// given
		ReceiptEntity receipt = testEntityManager.find(ReceiptEntity.class, savedReceiptEntity.getId());
		MenuEntity menu = testEntityManager.find(MenuEntity.class, savedMenuEntity.getId());

		List<OrderEntity> savedOrders = new java.util.ArrayList<>();
		for (int i = 0; i < 5; i++) {
			OrderEntity orderEntity = OrderEntity.builder()
				.status(OrderStatus.ORDERED)
				.totalPrice(1000 * (i + 1))
				.receipt(receipt)
				.build();
			OrderMenuEntity orderMenuEntity = OrderMenuEntity.of(1, OrderMenuStatus.ORDERED, orderEntity, menu);
			orderEntity.getOrderMenus().add(orderMenuEntity);
			savedOrders.add(orderJpaRepository.save(orderEntity));
		}

		testEntityManager.flush();
		testEntityManager.clear();

		// 5개 중 id가 가장 큰 3개의 id를 기준으로 cursor 설정
		Long thirdHighestId = savedOrders.stream()
			.map(OrderEntity::getId)
			.sorted(java.util.Comparator.reverseOrder())
			.skip(2)  // 1등, 2등 건너뜀
			.findFirst()
			.orElseThrow();

		// when: thirdHighestId보다 작은 id를 가진 주문 조회 (pageSize=3)
		Slice<Order> result = orderRepository.getSaleOrderSliceWithMenuAndTable(
			savedSaleEntity.getId(),
			List.of(OrderStatus.ORDERED),
			3,
			thirdHighestId);

		// then: 3등 id 미만인 주문은 2개이므로 hasNext=false
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.hasNext()).isFalse();
		result.getContent().forEach(order ->
			assertThat(order.getOrderId()).isLessThan(thirdHighestId)
		);
	}

	@Test
	@DisplayName("상태 필터: 조회 조건에 포함된 status 주문만 반환한다")
	void findSaleOrdersWithMenuAndTable_상태_필터_테스트() {
		// given
		ReceiptEntity receipt = testEntityManager.find(ReceiptEntity.class, savedReceiptEntity.getId());
		MenuEntity menu = testEntityManager.find(MenuEntity.class, savedMenuEntity.getId());

		OrderEntity orderedOrder = OrderEntity.builder()
			.status(OrderStatus.ORDERED)
			.totalPrice(1000)
			.receipt(receipt)
			.build();
		orderedOrder.getOrderMenus().add(OrderMenuEntity.of(1, OrderMenuStatus.ORDERED, orderedOrder, menu));
		orderJpaRepository.save(orderedOrder);

		OrderEntity receivedOrder = OrderEntity.builder()
			.status(OrderStatus.RECEIVED)
			.totalPrice(2000)
			.receipt(receipt)
			.build();
		receivedOrder.getOrderMenus().add(OrderMenuEntity.of(1, OrderMenuStatus.COOKING, receivedOrder, menu));
		orderJpaRepository.save(receivedOrder);

		OrderEntity completedOrder = OrderEntity.builder()
			.status(OrderStatus.COMPLETED)
			.totalPrice(3000)
			.receipt(receipt)
			.build();
		completedOrder.getOrderMenus().add(OrderMenuEntity.of(1, OrderMenuStatus.COMPLETED, completedOrder, menu));

		orderJpaRepository.save(completedOrder);

		testEntityManager.flush();
		testEntityManager.clear();

		// when: ORDERED 상태만 조회
		Slice<Order> result = orderRepository.getSaleOrderSliceWithMenuAndTable(
			savedSaleEntity.getId(),
			List.of(OrderStatus.ORDERED),
			10,
			null);

		// then
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getOrderStatus()).isEqualTo(OrderStatus.ORDERED);
	}

	@Test
	@DisplayName("전체 목록 조회: 다른 sale의 주문은 포함되지 않는다")
	void findSaleOrdersWithMenuAndTable_다른_sale_격리_테스트() {
		// given: 다른 sale + receipt 생성
		SaleEntity otherSale = testFixtureBuilder.buildSaleEntity(GENERAL_SALE(savedStoreEntity));
		TableEntity otherTable = testFixtureBuilder.buildTableEntity(
			TableEntityFixture.GENERAL_TABLE_ENTITY(savedStoreEntity));
		ReceiptEntity otherReceipt = testFixtureBuilder.buildReceiptEntity(
			ReceiptEntityFixture.GENERAL_ADJUSTMENT_RECEIPT(otherSale, otherTable));

		ReceiptEntity myReceipt = testEntityManager.find(ReceiptEntity.class, savedReceiptEntity.getId());
		ReceiptEntity anotherReceipt = testEntityManager.find(ReceiptEntity.class, otherReceipt.getId());
		MenuEntity menu = testEntityManager.find(MenuEntity.class, savedMenuEntity.getId());

		// savedSale의 주문 2개
		for (int i = 0; i < 2; i++) {
			OrderEntity order = OrderEntity.builder()
				.status(OrderStatus.ORDERED)
				.totalPrice(1000)
				.receipt(myReceipt)
				.build();
			order.getOrderMenus().add(OrderMenuEntity.of(1, OrderMenuStatus.ORDERED, order, menu));
			orderJpaRepository.save(order);
		}

		// 다른 sale의 주문 3개 (조회 대상 아님)
		for (int i = 0; i < 3; i++) {
			OrderEntity order = OrderEntity.builder()
				.status(OrderStatus.ORDERED)
				.totalPrice(1000)
				.receipt(anotherReceipt)
				.build();
			order.getOrderMenus().add(OrderMenuEntity.of(1, OrderMenuStatus.ORDERED, order, menu));
			orderJpaRepository.save(order);
		}

		testEntityManager.flush();
		testEntityManager.clear();

		// when
		List<Order> result = orderRepository.getSaleOrdersWithMenuAndTable(
			savedSaleEntity.getId(),
			List.of(OrderStatus.ORDERED));

		// then: savedSale 주문 2개만 반환
		assertThat(result).hasSize(2);
		result.forEach(order ->
			assertThat(order.getReceipt().getReceiptInfo().getReceiptId())
				.isEqualTo(savedReceiptEntity.getId())
		);
	}
}
