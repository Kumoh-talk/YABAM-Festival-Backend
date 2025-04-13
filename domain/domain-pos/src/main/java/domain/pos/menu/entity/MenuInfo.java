package domain.pos.menu.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MenuInfo {
	private Long menuId;
	private int menuOrder;
	private String menuName;
	private int price;
	private String description;
	private String imageUrl;
	private boolean isSoldOut;
	private boolean isRecommended;
	// TODO : 인프라 단에서 menuCategoryId와 menuOrder 복합 Unique 제약조건으로 설정

	@Builder
	public MenuInfo(Long menuId, int menuOrder, String menuName, int price, String description, String imageUrl,
		boolean isSoldOut, boolean isRecommended) {
		this.menuId = menuId;
		this.menuOrder = menuOrder;
		this.menuName = menuName;
		this.price = price;
		this.description = description;
		this.imageUrl = imageUrl;
		this.isSoldOut = isSoldOut;
		this.isRecommended = isRecommended;
	}
}
