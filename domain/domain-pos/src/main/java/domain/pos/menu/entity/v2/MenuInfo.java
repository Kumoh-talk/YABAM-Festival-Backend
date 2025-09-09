package domain.pos.menu.entity.v2;

import java.util.regex.Pattern;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.menu.entity.v2.state.MenuInfoState;
import io.micrometer.common.util.StringUtils;
import lombok.Value;

@Value
class MenuInfo implements MenuInfoState {
	String name;
	Integer price;
	String description;
	String imageUrl;

	private static final Pattern IMAGE_URL_PATTERN = Pattern.compile("^(https?://).*");

	MenuInfo(String name, Integer price, String description, String imageUrl) {
		checkStates(name, price, imageUrl);
		this.name = name;
		this.price = price;
		this.description = description;
		this.imageUrl = imageUrl;
	}

	MenuInfo(MenuInfoState menuInfoState) {
		this(
			menuInfoState.getName(),
			menuInfoState.getPrice(),
			menuInfoState.getDescription(),
			menuInfoState.getImageUrl()
		);
	}

	private void checkStates(String name, Integer price, String imageUrl) {
		checkNameRule(name);
		checkPriceRule(price);
		checkImageUrlRule(imageUrl);
	}

	private void checkNameRule(String name) {
		if (StringUtils.isBlank(name)) {
			throw new ServiceException(ErrorCode.DOMAIN_INVALID_MENU_NAME);
		}
	}

	private void checkPriceRule(Integer price) {
		if (price == null || price < 0) {
			throw new ServiceException(ErrorCode.DOMAIN_INVALID_MENU_PRICE);
		}
	}

	private void checkImageUrlRule(String imageUrl) {
		if (imageUrl != null && !IMAGE_URL_PATTERN.matcher(imageUrl).matches()) {
			throw new ServiceException(ErrorCode.DOMAIN_INVALID_MENU_IMAGE_URL);
		}
	}
}
