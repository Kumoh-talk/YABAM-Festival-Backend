package domain.pos.menu.entity.v2.domain;

import static java.util.Objects.*;

import java.util.Objects;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.AuditStamp;

import domain.pos.menu.entity.v2.domain.state.MenuInfoState;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Menu {
	private Long id;
	private MenuInfoState menuInfo;
	private Integer order;
	private boolean isSoldOut;
	private boolean isRecommended;

	private AuditStamp auditStamp;

	private Long storeId;
	private Long menuCategoryId;

	@Builder
	private Menu(Long id, MenuInfoState menuInfo, Integer order,
		boolean isSoldOut, boolean isRecommended, AuditStamp auditStamp, Long storeId, Long menuCategoryId) {
		this.id = id;
		this.menuInfo = menuInfo;
		this.order = order;
		this.isSoldOut = isSoldOut;
		this.isRecommended = isRecommended;
		this.auditStamp = auditStamp;
		this.storeId = storeId;
		this.menuCategoryId = menuCategoryId;
	}

	public static Menu create(MenuInfoState createMenuInfoState, Integer order,
		Long storeId, Long menuCategoryId) {
		checkOrderRule(order);
		return Menu.builder()
			.id(null)
			.menuInfo(MenuInfo.of(createMenuInfoState))
			.order(order)
			.isSoldOut(false)
			.isRecommended(false)
			.storeId(requireNonNull(storeId))
			.menuCategoryId(requireNonNull(menuCategoryId))
			.build();
	}

	public boolean updateMenuInfo(MenuInfoState updateMenuInfoState) {
		MenuInfoState newMenuInfo = MenuInfo.of(updateMenuInfoState);
		if (this.menuInfo.equals(newMenuInfo)) {
			return false;
		}
		this.menuInfo = newMenuInfo;
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

	public boolean updateIsSoldOut(Boolean isSoldOut) {
		requireNonNull(isSoldOut);
		if (this.isSoldOut == isSoldOut) {
			return false;
		}
		this.isSoldOut = isSoldOut;
		return true;
	}

	public boolean updateIsRecommended(Boolean isRecommended) {
		requireNonNull(isRecommended);
		if (this.isRecommended == isRecommended) {
			return false;
		}
		this.isRecommended = isRecommended;
		return true;
	}

	private static void checkOrderRule(Integer order) {
		if (order == null || order < 1) {
			throw new ServiceException(ErrorCode.DOMAIN_INVALID_MENU_ORDER);
		}
	}

	public static Menu fromInfra(Long id,
		MenuInfoState menuInfoState,
		Integer order, boolean isSoldOut, boolean isRecommended,
		AuditStamp auditStamp, Long storeId, Long menuCategoryId) {
		return Menu.builder()
			.id(id)
			.menuInfo(MenuInfo.of(menuInfoState))
			.order(order)
			.isSoldOut(isSoldOut)
			.isRecommended(isRecommended)
			.auditStamp(auditStamp)
			.storeId(storeId)
			.menuCategoryId(menuCategoryId)
			.build();
	}
}
