package domain.pos.menu.entity.v2.domain;

import static java.util.Objects.*;

import java.util.Objects;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import io.micrometer.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MenuCategory {
	private Long id;
	private String name;
	private Integer order;

	private Long storeId;

	@Builder(access = AccessLevel.PRIVATE)
	private MenuCategory(Long id, String name, Integer order, Long storeId) {
		this.id = id;
		this.name = name;
		this.order = order;
		this.storeId = storeId;
	}

	public static MenuCategory create(String name, Integer order, Long storeId) {
		checkStates(name, order);
		if (isValidOrder(order)) {
			throw new IllegalArgumentException("카테고리 생성 시 주문 순서가 올바르지 않습니다.");
		}

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
		} else {
			this.name = updateName;
			return true;
		}
	}

	public boolean updateOrder(Integer updateOrder) {
		if (isValidOrder(updateOrder)) {
			throw new ServiceException(ErrorCode.DOMAIN_INVALID_MENU_CATEGORY_ORDER);
		}

		if (Objects.equals(this.order, updateOrder)) {
			return false;
		} else {
			this.order = updateOrder;
			return true;
		}
	}

	private static void checkStates(String name, Integer order) {
		checkNameRule(name);
	}

	private static void checkNameRule(String name) {
		if (StringUtils.isBlank(name)) {
			throw new ServiceException(ErrorCode.DOMAIN_INVALID_MENU_CATEGORY_NAME);
		}
	}

	private static boolean isValidOrder(Integer order) {
		return order != null && order >= 1;
	}

	public static MenuCategory fromInfra(Long id, String name, Integer order, Long storeId) {
		return MenuCategory.builder()
			.id(id)
			.name(name)
			.order(order)
			.storeId(storeId)
			.build();
	}

}
