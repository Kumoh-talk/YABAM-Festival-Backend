package domain.pos.review.service;

import static fixtures.receipt.ReceiptFixture.*;
import static fixtures.review.ReviewFixture.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

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

class ReviewServiceTest extends ServiceTest {

	@Mock
	private ReviewWriter reviewWriter;

	@Mock
	private ReceiptReader receiptReader;

	@Mock
	private ReviewReader reviewReader;

	@InjectMocks
	private ReviewService reviewService;

	@Nested
	class CreateReview {
		@Test
		void 성공() {
			// given
			ReceiptInfo savedReceiptInfo = GENERAL_ADJUSTMENT_RECEIPT().getReceiptInfo();
			Review responReview = GENERAL_REVIEW(GENERAL_ADJUSTMENT_RECEIPT());
			Long queryReceiptId = GENERAL_ADJUSTMENT_RECEIPT().getReceiptInfo().getReceiptId();
			UserPassport queryUserPassport = responReview.getUserPassport();
			ReviewInfo queryReviewInfo = responReview.getReviewInfo();
			boolean isNonExistReceipt = false;

			doReturn(Optional.ofNullable(savedReceiptInfo))
				.when(receiptReader).getReceiptInfo(anyLong());
			doReturn(responReview)
				.when(reviewWriter).postReview(any(UserPassport.class), any(ReceiptInfo.class), any(ReviewInfo.class));
			doReturn(isNonExistReceipt)
				.when(reviewReader).isExitsReview(anyLong(), any(UserPassport.class));
			// when
			Review review = reviewService.postReview(queryUserPassport, queryReceiptId, queryReviewInfo);

			// then
			assertSoftly(softly -> {
				verify(receiptReader)
					.getReceiptInfo(anyLong());
				verify(reviewReader)
					.isExitsReview(anyLong(), any(UserPassport.class));
				verify(reviewWriter)
					.postReview(any(UserPassport.class), any(ReceiptInfo.class), any(ReviewInfo.class));
				softly.assertThat(review).isEqualTo(responReview);
			});
		}

		@Test
		void 실패_유효하지않은_영수증() {
			// given
			Review responReview = GENERAL_REVIEW(GENERAL_ADJUSTMENT_RECEIPT());
			Long queryReceiptId = GENERAL_ADJUSTMENT_RECEIPT().getReceiptInfo().getReceiptId();
			UserPassport queryUserPassport = responReview.getUserPassport();
			ReviewInfo queryReviewInfo = responReview.getReviewInfo();
			doReturn(Optional.empty())
				.when(receiptReader).getReceiptInfo(anyLong());

			// when->then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> reviewService.postReview(queryUserPassport, queryReceiptId, queryReviewInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECEIPT_NOT_FOUND);
				verify(receiptReader)
					.getReceiptInfo(anyLong());
				verify(reviewReader, never())
					.isExitsReview(anyLong(), any(UserPassport.class));
				verify(reviewWriter, never())
					.postReview(any(UserPassport.class), any(ReceiptInfo.class), any(ReviewInfo.class));
			});
		}

		@Test
		void 실패_영수증이_정산이_안됨() {
			// given
			Receipt savedReceipt = GENERAL_NON_ADJUSTMENT_RECEIPT();
			ReceiptInfo savedReceiptInfo = savedReceipt.getReceiptInfo();
			Review responReview = GENERAL_REVIEW(savedReceipt);
			Long queryReceiptId = savedReceipt.getReceiptInfo().getReceiptId();
			UserPassport queryUserPassport = responReview.getUserPassport();
			ReviewInfo queryReviewInfo = responReview.getReviewInfo();

			doReturn(Optional.ofNullable(savedReceiptInfo))
				.when(receiptReader).getReceiptInfo(anyLong());

			// when->then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> reviewService.postReview(queryUserPassport, queryReceiptId, queryReviewInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_ADJUSTMENT);
				verify(receiptReader)
					.getReceiptInfo(anyLong());
				verify(reviewReader, never())
					.isExitsReview(anyLong(), any(UserPassport.class));
				verify(reviewWriter, never())
					.postReview(any(UserPassport.class), any(ReceiptInfo.class), any(ReviewInfo.class));
			});
		}

		@Test
		void 실패_이미_영수증이_있음() {
			// given
			Receipt savedReceipt = GENERAL_ADJUSTMENT_RECEIPT();
			ReceiptInfo savedReceiptInfo = savedReceipt.getReceiptInfo();
			Review responReview = GENERAL_REVIEW(savedReceipt);
			Long queryReceiptId = savedReceipt.getReceiptInfo().getReceiptId();
			UserPassport queryUserPassport = responReview.getUserPassport();
			ReviewInfo queryReviewInfo = responReview.getReviewInfo();
			boolean isExistReceipt = true;

			doReturn(Optional.ofNullable(savedReceiptInfo))
				.when(receiptReader).getReceiptInfo(anyLong());
			doReturn(isExistReceipt)
				.when(reviewReader).isExitsReview(anyLong(), any(UserPassport.class));

			// when->then
			assertSoftly(softly -> {
				softly.assertThatThrownBy(
						() -> reviewService.postReview(queryUserPassport, queryReceiptId, queryReviewInfo))
					.isInstanceOf(ServiceException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_ALREADY_EXITS);
				verify(receiptReader)
					.getReceiptInfo(anyLong());
				verify(reviewReader)
					.isExitsReview(anyLong(), any(UserPassport.class));
				verify(reviewWriter, never())
					.postReview(any(UserPassport.class), any(ReceiptInfo.class), any(ReviewInfo.class));
			});
		}

	}

}
