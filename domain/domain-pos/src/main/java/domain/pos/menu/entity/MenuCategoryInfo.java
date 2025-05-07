package domain.pos.menu.entity;

import lombok.Getter;

@Getter
public class MenuCategoryInfo {
	private Long id;
	private String name;
	private Integer order;

	private MenuCategoryInfo(Long id, String name, Integer order) {
		this.id = id;
		this.name = name;
		this.order = order;
	}

	public static MenuCategoryInfo of(Long id, String name, Integer order) {
		return new MenuCategoryInfo(id, name, order);
	}
}
