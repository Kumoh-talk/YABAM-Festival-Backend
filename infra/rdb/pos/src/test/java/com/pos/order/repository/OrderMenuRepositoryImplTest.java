package com.pos.order.repository.impl;

import static com.pos.fixtures.sale.SaleFixture.GENERAL_SALE;
import static com.pos.fixtures.store.StoreEntityFixture.CUSTOME_STORE_ENTITY;
import static fixtures.store.StoreFixture.GENERAL_CLOSE_STORE;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.pos.fixtures.table.TableEntityFixture;
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
import com.pos.receipt.ReceiptEntityFixture;

import domain.pos.order.entity.OrderMenu;
import domain.pos.order.entity.vo.OrderMenuStatus;
import domain.pos.order.entity.vo.OrderStatus;
import domain.pos.order.repository.OrderMenuRepository;

class OrderMenuRepositoryImplTest extends RepositoryTest {

    @Autowired
    private OrderMenuRepository orderMenuRepository;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    private StoreEntity savedStoreEntity;
    private SaleEntity savedSaleEntity;
    private TableEntity savedTableEntity;
    private ReceiptEntity savedReceiptEntity;
    private MenuEntity savedMenuEntity;
    private OrderEntity savedOrderEntity;
    private OrderMenuEntity savedOrderMenuEntity;

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

        OrderEntity orderEntity = OrderEntity.builder()
                .status(OrderStatus.ORDERED)
                .totalPrice(20000)
                .receipt(savedReceiptEntity)
                .build();
        savedOrderEntity = orderJpaRepository.save(orderEntity);

        OrderMenuEntity orderMenuEntityToSave = OrderMenuEntity.of(2, OrderMenuStatus.ORDERED, savedOrderEntity,
                savedMenuEntity);
        testEntityManager.persist(orderMenuEntityToSave);
        savedOrderMenuEntity = orderMenuEntityToSave;

        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Test
    void patchOrderMenuQuantity_수량_증가_시_주문금액_증가_테스트() {
        // given
        OrderMenu orderMenu = orderMenuRepository
                .getOrderMenuWithOrderAndStoreAndOrderLock(savedOrderMenuEntity.getId())
                .orElseThrow();
        Integer originalTotalPrice = orderMenu.getOrder().getTotalPrice();
        int addQuantity = 2; // from 2 to 4
        int expectedPriceAdded = savedMenuEntity.getPrice() * addQuantity;

        // when
        orderMenuRepository.patchOrderMenuQuantity(orderMenu, 4);

        testEntityManager.flush();
        testEntityManager.clear();

        // then
        OrderEntity updatedOrder = testEntityManager.find(OrderEntity.class, savedOrderEntity.getId());
        assertThat(updatedOrder.getTotalPrice()).isEqualTo(originalTotalPrice + expectedPriceAdded);
    }

    @Test
    void patchOrderMenuQuantity_수량_감소_시_주문금액_감소_테스트() {
        // given
        OrderMenu orderMenu = orderMenuRepository
                .getOrderMenuWithOrderAndStoreAndOrderLock(savedOrderMenuEntity.getId())
                .orElseThrow();
        Integer originalTotalPrice = orderMenu.getOrder().getTotalPrice();
        int subtractQuantity = 1; // from 2 to 1
        int expectedPriceSubtracted = savedMenuEntity.getPrice() * subtractQuantity;

        // when
        orderMenuRepository.patchOrderMenuQuantity(orderMenu, 1);

        testEntityManager.flush();
        testEntityManager.clear();

        // then
        OrderEntity updatedOrder = testEntityManager.find(OrderEntity.class, savedOrderEntity.getId());
        assertThat(updatedOrder.getTotalPrice()).isEqualTo(originalTotalPrice - expectedPriceSubtracted);
    }

    @Test
    void patchOrderMenuStatus_CANCELED_상태로_변경시_주문금액_감소_테스트() {
        // given
        OrderMenu orderMenu = orderMenuRepository
                .getOrderMenuWithOrderAndStoreAndOrderLock(savedOrderMenuEntity.getId())
                .orElseThrow();
        Integer originalTotalPrice = orderMenu.getOrder().getTotalPrice();
        int expectedPriceSubtracted = savedMenuEntity.getPrice() * orderMenu.getQuantity();

        // when
        orderMenuRepository.patchOrderMenuStatus(orderMenu, OrderMenuStatus.CANCELED);

        testEntityManager.flush();
        testEntityManager.clear();

        // then
        OrderEntity updatedOrder = testEntityManager.find(OrderEntity.class, savedOrderEntity.getId());
        assertThat(updatedOrder.getTotalPrice()).isEqualTo(originalTotalPrice - expectedPriceSubtracted);

        OrderMenuEntity updatedOrderMenu = testEntityManager.find(OrderMenuEntity.class, savedOrderMenuEntity.getId());
        assertThat(updatedOrderMenu.getStatus()).isEqualTo(OrderMenuStatus.CANCELED);
    }

    @Test
    void deleteOrderMenu_삭제시_주문금액_차감_테스트() {
        // given
        OrderMenu orderMenu = orderMenuRepository
                .getOrderMenuWithOrderAndStoreAndOrderLock(savedOrderMenuEntity.getId())
                .orElseThrow();
        Integer originalTotalPrice = orderMenu.getOrder().getTotalPrice();
        int expectedPriceSubtracted = savedMenuEntity.getPrice() * orderMenu.getQuantity();

        // when
        orderMenuRepository.deleteOrderMenu(orderMenu);

        testEntityManager.flush();
        testEntityManager.clear();

        // then
        OrderEntity updatedOrder = testEntityManager.find(OrderEntity.class, savedOrderEntity.getId());
        assertThat(updatedOrder.getTotalPrice()).isEqualTo(originalTotalPrice - expectedPriceSubtracted);

        OrderMenuEntity deletedOrderMenu = testEntityManager.find(OrderMenuEntity.class, savedOrderMenuEntity.getId());
        assertThat(deletedOrderMenu).isNull();
    }

}
