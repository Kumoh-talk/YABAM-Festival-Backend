package domain.pos.store.entity;

import static com.exception.ErrorCode.*;
import static fixtures.store.DetailImagesFixture.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.exception.ServiceException;

class DetailImagesTest {

	@Test
	void createDetailImageTest() {
		var imageUrl = "https://example.com/test_image.jpg";
		var detailImages = DETAIL_IMAGES_FIXTURE();

		detailImages.register(imageUrl);

		assertThat(detailImages.getImageUrls().get(detailImages.getImageUrls().size() - 1)).isEqualTo(imageUrl);
	}

	@Test
	void registerFailTest() {
		var imageUrl = "hts://example.com/test_image.jpg";
		var detailImages = DETAIL_IMAGES_FIXTURE();

		assertThatThrownBy(() ->
			detailImages.register(imageUrl)
		).isInstanceOf(ServiceException.class)
			.hasFieldOrPropertyWithValue("errorCode", NOT_VALID_VO);
	}

	@Test
	void deleteDetailImageTest() {
		var detailImages = DETAIL_IMAGES_FIXTURE();
		var imageUrl = detailImages.getImageUrls().get(0);

		detailImages.remove(imageUrl);

		assertThat(detailImages.getImageUrls()).doesNotContain(imageUrl);
	}

	@Test
	void deleteDetailImageFailTest() {
		var detailImages = DETAIL_IMAGES_FIXTURE();
		var imageUrl = "https://example.com/test_image.jpg";
		var notValidateImageUrl = "tps://example.com/test_image.jpg";

		assertThatThrownBy(() ->
			detailImages.remove(notValidateImageUrl)
		)
			.isInstanceOf(ServiceException.class)
			.hasFieldOrPropertyWithValue("errorCode", NOT_VALID_VO);

		assertThatThrownBy(() ->
			detailImages.remove(imageUrl)
		)
			.isInstanceOf(ServiceException.class)
			.hasFieldOrPropertyWithValue("errorCode", NOT_FOUND_STORE_DETAIL_IMAGE);

	}

}
