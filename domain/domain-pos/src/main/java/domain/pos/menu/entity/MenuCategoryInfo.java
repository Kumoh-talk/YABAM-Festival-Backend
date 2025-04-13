package domain.pos.menu.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MenuCategoryInfo {
	private Long menuCategoryId;
	private String menuCategoryName;
	private int menuCategoryOrder;
	// TODO : 인프라 단에서 storeId와 menuCategoryOrder를 복합 Unique 제약조건으로 설정

	@Builder
	public MenuCategoryInfo(Long menuCategoryId, String menuCategoryName, int menuCategoryOrder) {
		this.menuCategoryId = menuCategoryId;
		this.menuCategoryName = menuCategoryName;
		this.menuCategoryOrder = menuCategoryOrder;
	}
}
