package domain.pos.menu.entity.v2;

import static java.util.Objects.*;

import java.time.LocalDateTime;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.AuditStamp;

import domain.pos.menu.entity.v2.state.MenuInfoState;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Menu {
	private Long id;
	private MenuInfo menuInfo;
	private Integer order;
	private boolean isSoldOut;
	private boolean isRecommended;

	private AuditStamp auditStamp;

	private Long storeId;
	private Long menuCategoryId;

	@Builder
	private Menu(Long id, MenuInfo menuInfo, Integer order,
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
		MenuInfo menuInfo = new MenuInfo(createMenuInfoState);
		checkOrder(order);
		return Menu.builder()
			.id(null)
			.menuInfo(menuInfo)
			.order(order)
			.isSoldOut(false)
			.isRecommended(false)
			.storeId(requireNonNull(storeId))
			.menuCategoryId(requireNonNull(menuCategoryId))
			.build();
	}

	public boolean updateMenuInfo(MenuInfoState updateMenuInfoState) {
		MenuInfo newMenuInfo = new MenuInfo(updateMenuInfoState);
		if (this.menuInfo.equals(newMenuInfo)) {
			return false;
		} else {
			this.menuInfo = newMenuInfo;
			return true;
		}
	}

	public boolean updateOrder(Integer updateOrder) {
		checkOrder(updateOrder);
		if (this.order.equals(updateOrder)) {
			return false;
		} else {
			this.order = updateOrder;
			return true;
		}
	}

	public boolean updateIsSoldOut(boolean isSoldOut) {
		if (this.isSoldOut == isSoldOut) {
			return false;
		} else {
			this.isSoldOut = isSoldOut;
			return true;
		}
	}

	public boolean updateIsRecommended(boolean isRecommended) {
		if (this.isRecommended == isRecommended) {
			return false;
		} else {
			this.isRecommended = isRecommended;
			return true;
		}
	}

	private static void checkOrder(Integer order) {
		if (order == null || order < 1) {
			throw new ServiceException(ErrorCode.DOMAIN_INVALID_MENU_ORDER);
		}
	}

	// util
	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.auditStamp = this.auditStamp.update(updatedAt);
	}

	public static Menu fromInfra(Long id,
		MenuInfoState menuInfoState,
		Integer order, boolean isSoldOut, boolean isRecommended,
		LocalDateTime createdAt, Long storeId, Long menuCategoryId) {
		return Menu.builder()
			.id(id)
			.menuInfo(new MenuInfo(menuInfoState))
			.order(order)
			.isSoldOut(isSoldOut)
			.isRecommended(isRecommended)
			.auditStamp(new AuditStamp(createdAt, null, null))
			.storeId(storeId)
			.menuCategoryId(menuCategoryId)
			.build();
	}
}
