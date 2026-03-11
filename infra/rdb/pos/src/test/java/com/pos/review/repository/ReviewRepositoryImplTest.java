package com.pos.review.repository;

import static com.pos.fixtures.sale.SaleFixture.*;
import static com.pos.fixtures.store.StoreEntityFixture.*;
import static fixtures.store.StoreFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.exception.ServiceException;
import com.pos.global.config.RepositoryTest;
import com.pos.receipt.entity.ReceiptEntity;
import com.pos.review.entity.ReviewEntity;
import com.pos.review.repository.jpa.ReviewJpaRepository;
import com.pos.sale.entity.SaleEntity;
import com.pos.store.entity.StoreEntity;
import com.pos.table.entity.TableEntity;
import com.pos.receipt.ReceiptEntityFixture;
import com.pos.fixtures.table.TableEntityFixture;

import domain.pos.review.entity.Review;
import domain.pos.review.entity.ReviewInfo;
import domain.pos.review.repository.ReviewRepository;
import com.vo.UserPassport;

class ReviewRepositoryImplTest extends RepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewJpaRepository reviewJpaRepository;

    private StoreEntity savedStoreEntity;
    private SaleEntity savedSaleEntity;
    private TableEntity savedTableEntity;
    private ReceiptEntity savedReceiptEntity;
    private ReviewEntity savedReviewEntity;

    @BeforeEach
    void setUp() {
        savedStoreEntity = testFixtureBuilder.buildStoreEntity(CUSTOME_STORE_ENTITY(GENERAL_CLOSE_STORE()));
        savedSaleEntity = testFixtureBuilder.buildSaleEntity(GENERAL_SALE(savedStoreEntity));
        savedTableEntity = testFixtureBuilder.buildTableEntity(
                TableEntityFixture.GENERAL_TABLE_ENTITY(savedStoreEntity));

        savedReceiptEntity = testFixtureBuilder.buildReceiptEntity(
                ReceiptEntityFixture.GENERAL_ADJUSTMENT_RECEIPT(savedSaleEntity, savedTableEntity));

        savedReviewEntity = ReviewEntity.of(com.pos.review.entity.vo.ReviewUser.of(100L, "Tester100"),
                "Great test!", 5, savedStoreEntity, savedReceiptEntity);
        reviewJpaRepository.save(savedReviewEntity);

        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Test
    void findById_존재하는_리뷰_조회_테스트() {
        // when
        Optional<Review> review = reviewRepository.findById(savedReviewEntity.getId());

        // then
        assertThat(review).isPresent();
        assertThat(review.get().getReviewId()).isEqualTo(savedReviewEntity.getId());
        assertThat(review.get().getReviewInfo().getContent()).isEqualTo("Great test!");
    }

    @Test
    void findById_존재하지_않는_리뷰_조회_테스트() {
        // when
        Optional<Review> review = reviewRepository.findById(9999L);

        // then
        assertThat(review).isEmpty();
    }

    @Test
    void updateReview_정상_업데이트_테스트() {
        // given
        Review review = reviewRepository.findById(savedReviewEntity.getId()).orElseThrow();
        ReviewInfo updateInfo = ReviewInfo.of("Updated!", 4);

        // when
        Review updated = reviewRepository.updateReview(review, updateInfo);
        testEntityManager.flush();
        testEntityManager.clear();

        // then
        ReviewEntity entity = reviewJpaRepository.findById(savedReviewEntity.getId()).orElseThrow();
        assertThat(entity.getContent()).isEqualTo("Updated!");
        assertThat(entity.getRating()).isEqualTo(4);
    }

    @Test
    void updateReview_존재하지_않는_리뷰_예외발생_테스트() {
        // given
        Review notFoundReview = domain.pos.review.entity.Review.of(9999L, ReviewInfo.of("Content", 5),
                com.vo.UserPassport.of(100L, "Tester100", com.vo.UserRole.ROLE_USER), null, null, null);
        ReviewInfo updateInfo = ReviewInfo.of("Updated!", 4);

        // when & then
        assertThatThrownBy(() -> reviewRepository.updateReview(notFoundReview, updateInfo))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    void deleteReview_정상_삭제_테스트() {
        // given
        Review review = reviewRepository.findById(savedReviewEntity.getId()).orElseThrow();

        // when
        reviewRepository.deleteReview(review);
        testEntityManager.flush();
        testEntityManager.clear();

        // then
        assertThat(reviewJpaRepository.findById(savedReviewEntity.getId())).isEmpty();
    }

}
