package domain.pos.review.service;

import static fixtures.member.UserFixture.*;
import static fixtures.receipt.ReceiptFixture.*;
import static fixtures.review.ReviewFixture.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import base.ServiceTest;
import domain.pos.member.entity.UserPassport;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.receipt.implement.ReceiptReader;
import domain.pos.review.entity.Review;
import domain.pos.review.entity.ReviewInfo;
import domain.pos.review.implement.ReviewReader;
import domain.pos.review.implement.ReviewWriter;
import domain.pos.store.implement.StoreValidator;

class ReviewServiceTest extends ServiceTest {

	@Mock
	private StoreValidator storeValidator;

	@Mock
	private ReviewWriter reviewWriter;

	@Mock
	private ReceiptReader receiptReader;

	@Mock
	private ReviewReader reviewReader;

	@InjectMocks
	private ReviewService reviewService;

	@Nested
	@DisplayName("리뷰 등록")
	class CreateReview {
		@Test
		void 성공() {
			// given
			ReceiptInfo savedReceiptInfo = GENERAL_ADJUSTMENT_RECEIPT().getReceiptInfo();
			Review responReview = GENERAL_REVIEW(GENERAL_ADJUSTMENT_RECEIPT());
			Long queryStoreId = responReview.getStore().getStoreId();
			Long queryReceiptId = GENERAL_ADJUSTMENT_RECEIPT().getReceiptInfo().getReceiptId();
			UserPassport queryUserPassport = responReview.getUserPassport();
			ReviewInfo queryReviewInfo = responReview.getReviewInfo();
			boolean isNonExistReceipt = false;

			doReturn(Optional.ofNullable(savedReceiptInfo))
				.when(receiptReader).getReceiptInfo(anyLong());
			doReturn(responReview)
				.when(reviewWriter)
				.postReview(any(UserPassport.class), anyLong(), any(ReceiptInfo.class), any(ReviewInfo.class));
			doReturn(isNonExistReceipt)
				.when(reviewReader).isExistsReview(anyLong(), any(UserPassport.class));
			// when
			Review review = reviewService.postReview(queryUserPassport, queryStoreId, queryReceiptId, queryReviewInfo);

			// then
			assertSoftly(softly -> {
				verify(storeValidator)
					.validateStore(anyLong());
				verify(receiptReader)
					.getReceiptInfo(anyLong());
				verify(reviewReader)
					.isExistsReview(anyLong(), any(UserPassport.class));
				verify(reviewWriter)
					.postReview(any(UserPassport.class), anyLong(), any(ReceiptInfo.class), any(ReviewInfo.class));
				softly.assertThat(review).isEqualTo(responReview);
			});
		}

		@Test
		void 실패_유효하지않은_가게() {
			// given
			Receipt responReceipt = GENERAL_ADJUSTMENT_RECEIPT();
			Review responReview = GENERAL_REVIEW(responReceipt);
			Long queryStoreId = responReview.getStore().getStoreId();
			Long queryReceiptId = responReceipt.getReceiptInfo().getReceiptId();
			UserPassport queryUserPassport = responReview.getUserPassport();
			ReviewInfo queryReviewInfo = responReview.getReviewInfo();

			doThrow(new ServiceException(ErrorCode.NOT_FOUND_STORE))
				.when(storeValidator).validateStore(anyLong());

			// when->then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> reviewService.postReview(queryUserPassport, queryStoreId, queryReceiptId, queryReviewInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_STORE);
				verify(storeValidator)
					.validateStore(anyLong());
				verify(receiptReader, never())
					.getReceiptInfo(anyLong());
				verify(reviewReader, never())
					.isExistsReview(anyLong(), any(UserPassport.class));
				verify(reviewWriter, never())
					.postReview(any(UserPassport.class), anyLong(), any(ReceiptInfo.class), any(ReviewInfo.class));
			});
		}

		@Test
		void 실패_유효하지않은_영수증() {
			// given
			Review responReview = GENERAL_REVIEW(GENERAL_ADJUSTMENT_RECEIPT());
			Long queryStoreId = responReview.getStore().getStoreId();
			Long queryReceiptId = GENERAL_ADJUSTMENT_RECEIPT().getReceiptInfo().getReceiptId();
			UserPassport queryUserPassport = responReview.getUserPassport();
			ReviewInfo queryReviewInfo = responReview.getReviewInfo();
			doReturn(Optional.empty())
				.when(receiptReader).getReceiptInfo(anyLong());

			// when->then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> reviewService.postReview(queryUserPassport, queryStoreId, queryReceiptId, queryReviewInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_NOT_FOUND);
				verify(storeValidator)
					.validateStore(anyLong());
				verify(receiptReader)
					.getReceiptInfo(anyLong());
				verify(reviewReader, never())
					.isExistsReview(anyLong(), any(UserPassport.class));
				verify(reviewWriter, never())
					.postReview(any(UserPassport.class), anyLong(), any(ReceiptInfo.class), any(ReviewInfo.class));
			});
		}

		@Test
		void 실패_영수증이_정산이_안됨() {
			// given
			Receipt savedReceipt = GENERAL_NON_ADJUSTMENT_RECEIPT();
			ReceiptInfo savedReceiptInfo = savedReceipt.getReceiptInfo();
			Review responReview = GENERAL_REVIEW(savedReceipt);
			Long queryStoreId = responReview.getStore().getStoreId();
			Long queryReceiptId = savedReceipt.getReceiptInfo().getReceiptId();
			UserPassport queryUserPassport = responReview.getUserPassport();
			ReviewInfo queryReviewInfo = responReview.getReviewInfo();

			doReturn(Optional.ofNullable(savedReceiptInfo))
				.when(receiptReader).getReceiptInfo(anyLong());

			// when->then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> reviewService.postReview(queryUserPassport, queryStoreId, queryReceiptId, queryReviewInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_ADJUSTMENT);
				verify(storeValidator)
					.validateStore(anyLong());
				verify(receiptReader)
					.getReceiptInfo(anyLong());
				verify(reviewReader, never())
					.isExistsReview(anyLong(), any(UserPassport.class));
				verify(reviewWriter, never())
					.postReview(any(UserPassport.class), anyLong(), any(ReceiptInfo.class), any(ReviewInfo.class));
			});
		}

		@Test
		void 실패_이미_영수증이_있음() {
			// given
			Receipt savedReceipt = GENERAL_ADJUSTMENT_RECEIPT();
			ReceiptInfo savedReceiptInfo = savedReceipt.getReceiptInfo();
			Review responReview = GENERAL_REVIEW(savedReceipt);
			Long queryStoreId = responReview.getStore().getStoreId();
			Long queryReceiptId = savedReceipt.getReceiptInfo().getReceiptId();
			UserPassport queryUserPassport = responReview.getUserPassport();
			ReviewInfo queryReviewInfo = responReview.getReviewInfo();
			boolean isExistReceipt = true;

			doReturn(Optional.ofNullable(savedReceiptInfo))
				.when(receiptReader).getReceiptInfo(anyLong());
			doReturn(isExistReceipt)
				.when(reviewReader).isExistsReview(anyLong(), any(UserPassport.class));

			// when->then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> reviewService.postReview(queryUserPassport, queryStoreId, queryReceiptId, queryReviewInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_ALREADY_EXISTS);
				verify(storeValidator)
					.validateStore(anyLong());
				verify(receiptReader)
					.getReceiptInfo(anyLong());
				verify(reviewReader)
					.isExistsReview(anyLong(), any(UserPassport.class));
				verify(reviewWriter, never())
					.postReview(any(UserPassport.class), anyLong(), any(ReceiptInfo.class), any(ReviewInfo.class));
			});
		}

	}

	@Nested
	@DisplayName("리뷰 수정")
	class updateReview {
		@Test
		void 성공() {
			// given
			Review savedReview = GENERAL_REVIEW(GENERAL_ADJUSTMENT_RECEIPT());
			ReviewInfo updateReviewInfo = GENERAL_REVIEW_INFO();
			Long queryReviewId = savedReview.getReviewId();
			UserPassport queryUserPassport = savedReview.getUserPassport();

			doReturn(Optional.ofNullable(savedReview))
				.when(reviewReader).getReview(anyLong());
			doReturn(savedReview)
				.when(reviewWriter).updateReview(any(Review.class), any(ReviewInfo.class));

			// when
			Review review = reviewService.updateReview(queryUserPassport, queryReviewId, updateReviewInfo);

			// then
			assertSoftly(softly -> {
				verify(reviewReader)
					.getReview(anyLong());
				verify(reviewWriter)
					.updateReview(any(Review.class), any(ReviewInfo.class));
				softly.assertThat(review).isEqualTo(savedReview);
			});
		}

		@Test
		void 실패_없는_리뷰_수정_시도() {
			// given
			Review savedReview = GENERAL_REVIEW(GENERAL_ADJUSTMENT_RECEIPT());
			ReviewInfo updateReviewInfo = GENERAL_REVIEW_INFO();
			Long queryReviewId = savedReview.getReviewId();
			UserPassport queryUserPassport = savedReview.getUserPassport();

			doReturn(Optional.empty())
				.when(reviewReader).getReview(anyLong());

			// when->then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> reviewService.updateReview(queryUserPassport, queryReviewId, updateReviewInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_FOUND);
				verify(reviewReader)
					.getReview(anyLong());
				verify(reviewWriter, never())
					.updateReview(any(Review.class), any(ReviewInfo.class));
			});
		}

		@Test
		void 실패_리뷰_소유자가_아닌_요청() {
			// given
			Review savedReview = GENERAL_REVIEW(GENERAL_ADJUSTMENT_RECEIPT());
			ReviewInfo updateReviewInfo = GENERAL_REVIEW_INFO();
			Long queryReviewId = savedReview.getReviewId();
			UserPassport queryUserPassport = DIFF_USER_PASSPORT();

			doReturn(Optional.of(savedReview))
				.when(reviewReader).getReview(anyLong());

			// when->then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> reviewService.updateReview(queryUserPassport, queryReviewId, updateReviewInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_USER);
				verify(reviewReader)
					.getReview(anyLong());
				verify(reviewWriter, never())
					.updateReview(any(Review.class), any(ReviewInfo.class));
			});
		}
	}

	@Nested
	@DisplayName("리뷰 삭제")
	class deleteReview {
		@Test
		void 성공() {
			// given
			Review savedReview = GENERAL_REVIEW(GENERAL_ADJUSTMENT_RECEIPT());
			Long queryReviewId = savedReview.getReviewId();
			UserPassport queryUserPassport = savedReview.getUserPassport();

			doReturn(Optional.ofNullable(savedReview))
				.when(reviewReader).getReview(anyLong());

			// when
			reviewService.deleteReview(queryUserPassport, queryReviewId);

			// then
			assertSoftly(softly -> {
				verify(reviewReader)
					.getReview(anyLong());
				verify(reviewWriter)
					.deleteReview(any(Review.class));
			});
		}

		@Test
		void 실패_없는_리뷰_삭제_시도() {
			// given
			Review savedReview = GENERAL_REVIEW(GENERAL_ADJUSTMENT_RECEIPT());
			Long queryReviewId = savedReview.getReviewId();
			UserPassport queryUserPassport = savedReview.getUserPassport();

			doReturn(Optional.empty())
				.when(reviewReader).getReview(anyLong());

			// when->then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> reviewService.deleteReview(queryUserPassport, queryReviewId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_FOUND);
				verify(reviewReader)
					.getReview(anyLong());
				verify(reviewWriter, never())
					.deleteReview(any(Review.class));
			});
		}

		@Test
		void 실패_리뷰_소유자가_아닌_요청() {
			// given
			Review savedReview = GENERAL_REVIEW(GENERAL_ADJUSTMENT_RECEIPT());
			Long queryReviewId = savedReview.getReviewId();
			UserPassport queryUserPassport = DIFF_USER_PASSPORT();

			doReturn(Optional.of(savedReview))
				.when(reviewReader).getReview(anyLong());

			// when->then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> reviewService.deleteReview(queryUserPassport, queryReviewId))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_USER);
				verify(reviewReader)
					.getReview(anyLong());
				verify(reviewWriter, never())
					.deleteReview(any(Review.class));
			});
		}
	}

}
