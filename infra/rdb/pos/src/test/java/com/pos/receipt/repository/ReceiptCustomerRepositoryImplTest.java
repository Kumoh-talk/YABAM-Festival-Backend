package com.pos.receipt.repository;

import static com.pos.fixtures.sale.SaleFixture.*;
import static com.pos.fixtures.store.StoreEntityFixture.*;
import static fixtures.store.StoreFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.pos.global.config.RepositoryTest;
import com.pos.receipt.entity.ReceiptCustomerEntity;
import com.pos.receipt.entity.ReceiptEntity;
import com.pos.receipt.repository.jpa.ReceiptCustomerJpaRepository;
import com.pos.sale.entity.SaleEntity;
import com.pos.store.entity.StoreEntity;
import com.pos.table.entity.TableEntity;
import com.pos.receipt.ReceiptEntityFixture;
import com.pos.fixtures.table.TableEntityFixture;

import domain.pos.receipt.repository.ReceiptCustomerRepository;

class ReceiptCustomerRepositoryImplTest extends RepositoryTest {

    @Autowired
    private ReceiptCustomerRepository receiptCustomerRepository;

    @Autowired
    private ReceiptCustomerJpaRepository receiptCustomerJpaRepository;

    private StoreEntity savedStoreEntity;
    private SaleEntity savedSaleEntity;
    private TableEntity savedTableEntity;
    private ReceiptEntity savedReceiptEntity;

    @BeforeEach
    void setUp() {
        savedStoreEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(GENERAL_CLOSE_STORE()));
        savedSaleEntity = testFixtureBuilder.buildSaleEntity(GENERAL_SALE(savedStoreEntity));
        savedTableEntity = testFixtureBuilder.buildTableEntity(
                TableEntityFixture.GENERAL_TABLE_ENTITY(savedStoreEntity));

        savedReceiptEntity = testFixtureBuilder.buildReceiptEntity(
                ReceiptEntityFixture.GENERAL_ADJUSTMENT_RECEIPT(savedSaleEntity, savedTableEntity));

        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Test
    void postReceiptCustomer_저장_테스트() {
        // given
        Long customerId = 100L;
        UUID receiptId = savedReceiptEntity.getId();

        // when
        receiptCustomerRepository.postReceiptCustomer(customerId, receiptId);
        testEntityManager.flush();
        testEntityManager.clear();

        // then
        long count = receiptCustomerJpaRepository.count();
        assertThat(count).isEqualTo(1L);
    }
}
