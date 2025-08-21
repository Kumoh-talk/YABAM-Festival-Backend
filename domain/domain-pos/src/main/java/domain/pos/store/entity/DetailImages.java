package domain.pos.store.entity;

import static com.exception.ErrorCode.*;
import static java.util.Objects.*;
import static lombok.AccessLevel.*;

import java.util.List;
import java.util.regex.Pattern;

import com.exception.ServiceException;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(of = {"storeId"})
@NoArgsConstructor(access = PRIVATE)
public class DetailImages {
	private static final Pattern IMAGE_URL_PATTERN = Pattern.compile("^(https?://).*");

	@Getter
	private Long storeId;

	private List<String> imageUrls;

	public List<String> getImageUrls() {
		return List.copyOf(imageUrls);
	}

	public static DetailImages of(Store store, List<String> imageUrl) {
		var detailImage = new DetailImages();

		detailImage.storeId = store.getId();
		detailImage.imageUrls = requireNonNull(imageUrl);

		return detailImage;
	}

	public void register(String imageUrl) {
		validatePattern(imageUrl);

		this.imageUrls.add(imageUrl);
	}

	private static void validatePattern(String imageUrl) {
		if (!IMAGE_URL_PATTERN.matcher(imageUrl).matches()) {
			throw new ServiceException(NOT_VALID_VO);
		}
	}

	public void remove(String imageUrl) {
		validatePattern(imageUrl);

		if (isDetailImageContain(imageUrl)) {
			throw new ServiceException(NOT_FOUND_STORE_DETAIL_IMAGE);
		}

		this.imageUrls.remove(imageUrl);
	}

	private boolean isDetailImageContain(String imageUrl) {
		return !this.imageUrls.contains(imageUrl);
	}
}
