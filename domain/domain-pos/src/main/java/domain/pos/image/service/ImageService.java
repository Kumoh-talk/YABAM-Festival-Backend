package domain.pos.image.service;

import org.springframework.stereotype.Service;

import com.url.UrlHandleUtil;
import com.vo.UserPassport;

import domain.pos.image.entity.ImageProperty;
import domain.pos.store.implement.StoreImageHandler;
import domain.pos.store.implement.StoreValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageService {
	private final StoreValidator storeValidator;
	private final StoreImageHandler storeImageHandler;

	public String getPresignedUrl(final UserPassport ownerPassport, final Long storeId,
		final ImageProperty imageProperty) {
		storeValidator.validateStoreOwner(ownerPassport, storeId);

		String url = switch (imageProperty) {
			case STORE_HEAD -> UrlHandleUtil.generateHeadUrl(storeId);
			case MENU_IMAGE -> UrlHandleUtil.generateStoreMenuUrl(storeId);
			default -> UrlHandleUtil.generateDetailUrl(storeId);
		};
		return storeImageHandler.generatePresignedUrl(url);
	}

}
