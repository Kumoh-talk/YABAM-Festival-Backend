package com.pos.receipt.repository;

import static com.pos.fixtures.sale.SaleFixture.*;
import static com.pos.fixtures.store.StoreEntityFixture.*;
import static fixtures.store.StoreFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.pos.global.config.RepositoryTest;
import com.pos.receipt.entity.ReceiptEntity;
import com.pos.receipt.repository.jpa.ReceiptJpaRepository;
import com.pos.sale.entity.SaleEntity;
import com.pos.store.entity.StoreEntity;
import com.pos.table.entity.TableEntity;

import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.repository.ReceiptRepository;
import domain.pos.store.entity.Store;
import domain.pos.store.entity.StoreInfo;

import com.pos.receipt.ReceiptEntityFixture;
import com.pos.fixtures.table.TableEntityFixture;
import fixtures.store.StoreInfoFixture;
import fixtures.member.UserFixture;

class ReceiptRepositoryImplTest extends RepositoryTest {

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private ReceiptJpaRepository receiptJpaRepository;

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
    void getReceiptById_정상_조회_테스트() {
        // when
        Optional<Receipt> result = receiptRepository.getReceiptById(savedReceiptEntity.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getReceiptInfo().getReceiptId()).isEqualTo(savedReceiptEntity.getId());
    }

    @Test
    void getReceiptById_존재하지_않는_ID_조회_테스트() {
        // when
        Optional<Receipt> result = receiptRepository.getReceiptById(UUID.randomUUID());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void deleteReceipt_정상_삭제_테스트() {
        // when
        receiptRepository.deleteReceipt(savedReceiptEntity.getId());

        testEntityManager.flush();
        testEntityManager.clear();

        // then
        Optional<ReceiptEntity> found = receiptJpaRepository.findById(savedReceiptEntity.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void deleteReceipt_존재하지_않는_ID_삭제_예외발생_테스트() {
        // given
        UUID unknownId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> receiptRepository.deleteReceipt(unknownId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void stopReceiptsWithMenu_존재하지_않는_영수증_예외발생_테스트() {
        // given
        UUID unknownId = UUID.randomUUID();
        Receipt notFoundReceipt = Receipt.of(
                domain.pos.receipt.entity.ReceiptInfo.builder().receiptId(unknownId).build(),
                null,
                null);

        // when & then
        assertThatThrownBy(() -> receiptRepository.stopReceiptsWithMenu(List.of(notFoundReceipt)))
                .isInstanceOf(NoSuchElementException.class);
    }
}
