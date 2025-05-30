package fixtures.review;

import static fixtures.store.StoreFixture.*;

import java.time.LocalDateTime;

import domain.pos.receipt.entity.Receipt;
import domain.pos.review.entity.Review;
import domain.pos.review.entity.ReviewInfo;
import domain.pos.store.entity.Store;
import fixtures.member.UserFixture;

public class ReviewFixture {
	private static final Long GENERAL_REVIEW_ID = 1L;
	private static final String GENERAL_REVIEW_CONTENT = "review content";
	private static final Integer GENERAL_REVIEW_RATING = 50;
	private static final LocalDateTime GENERAL_REVIEW_CREATED_AT = LocalDateTime.now();
	private static final Store GENERAL_STORE = GENERAL_CLOSE_STORE();

	public static ReviewInfo GENERAL_REVIEW_INFO() {
		return ReviewInfo.of(
			GENERAL_REVIEW_CONTENT,
			GENERAL_REVIEW_RATING
		);
	}

	public static Review GENERAL_REVIEW(Receipt receipt) {
		return Review.of(
			GENERAL_REVIEW_ID,
			GENERAL_REVIEW_INFO(),
			UserFixture.GENERAL_USER_PASSPORT(),
			receipt,
			GENERAL_STORE,
			GENERAL_REVIEW_CREATED_AT
		);
	}
}
