package fixtures.store;

import static fixtures.store.StoreFixture.*;

import java.util.ArrayList;
import java.util.List;

import domain.pos.store.entity.DetailImages;

public class DetailImagesFixture {
	public static DetailImages DETAIL_IMAGES_FIXTURE() {
		String imageUrl = "https://example.com/image.jpg";
		List<String> imageUrls = new ArrayList<>(List.of(imageUrl));

		return DetailImages.of(STORE_FIXTURE(), imageUrls);
	}
}
