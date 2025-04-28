package domain.pos.menu.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MenuInfo {
	private Long id;
	private Integer order;
	private String name;
	private Integer price;
	private String description;
	private String imageUrl;
	private boolean isSoldOut;
	private boolean isRecommended;

	@Builder
	public MenuInfo(Long id, Integer order, String name, Integer price, String description, String imageUrl,
		boolean isSoldOut, boolean isRecommended) {
		this.id = id;
		this.order = order;
		this.name = name;
		this.price = price;
		this.description = description;
		this.imageUrl = imageUrl;
		this.isSoldOut = isSoldOut;
		this.isRecommended = isRecommended;
	}
}
