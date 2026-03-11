package com.pos.order.repository;

import static com.pos.fixtures.sale.SaleFixture.*;
import static com.pos.fixtures.store.StoreEntityFixture.*;
import static fixtures.store.StoreFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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
import com.pos.fixtures.menu.MenuCategoryEntityFixture;
import fixtures.menu.MenuCategoryFixture;
import com.pos.fixtures.menu.MenuEntityFixture;
import fixtures.menu.MenuInfoFixture;
import fixtures.menu.MenuCategoryInfoFixture;

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
                                MenuEntity.of(fixtures.menu.MenuInfoFixture.REQUEST_MENU_INFO(), savedStoreEntity,
                                                menuCategory));

                testEntityManager.flush();
                testEntityManager.clear();
        }

        @Test
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

                        OrderMenuEntity orderMenuEntity = OrderMenuEntity.of(1, OrderMenuStatus.ORDERED, orderEntity,
                                        menu);

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
                assertThat(result.getContent()).hasSize(2); // pageSize가 2이므로 2개만 조회되어야 함
                assertThat(result.hasNext()).isTrue(); // 총 3개이므로 다음 페이지가 존재해야 함
                assertThat(result.getContent().get(0).getOrderMenus()).hasSize(1);
                assertThat(result.getContent().get(0).getOrderMenus().get(0).getMenu().getMenuInfo().getId())
                                .isEqualTo(savedMenuEntity.getId());
        }
}
