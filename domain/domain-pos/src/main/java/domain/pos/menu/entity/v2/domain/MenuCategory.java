package domain.pos.menu.entity.v2.domain;

import static java.util.Objects.*;

import java.util.Objects;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import io.micrometer.common.util.StringUtils;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MenuCategory {
	private Long id;
	private String name;
	private Integer order;

	private Long storeId;

	@Builder
	private MenuCategory(Long id, String name, Integer order, Long storeId) {
		this.id = id;
		this.name = name;
		this.order = order;
		this.storeId = storeId;
	}

	public static MenuCategory create(String name, Integer order, Long storeId) {
		checkStates(name, order);
		return MenuCategory.builder()
			.id(null)
			.name(name)
			.order(order)
			.storeId(requireNonNull(storeId)).build();
	}

	public boolean updateName(String updateName) {
		checkNameRule(updateName);
		if (Objects.equals(this.name, updateName)) {
			return false;
		}
		this.name = updateName;
		return true;
	}

	public boolean updateOrder(Integer updateOrder) {
		checkOrderRule(updateOrder);
		if (Objects.equals(this.order, updateOrder)) {
			return false;
		}
		this.order = updateOrder;
		return true;
	}

	private static void checkStates(String name, Integer order) {
		checkNameRule(name);
		checkOrderRule(order);
	}

	private static void checkNameRule(String name) {
		if (StringUtils.isBlank(name)) {
			throw new ServiceException(ErrorCode.DOMAIN_INVALID_MENU_CATEGORY_NAME);
		}
	}

	private static void checkOrderRule(Integer order) {
		if (order == null || order < 1) {
			throw new ServiceException(ErrorCode.DOMAIN_INVALID_MENU_CATEGORY_ORDER);
		}
	}

}
