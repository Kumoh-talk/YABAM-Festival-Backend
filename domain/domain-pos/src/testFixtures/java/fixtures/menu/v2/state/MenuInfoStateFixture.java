package fixtures.menu.v2.state;

import static fixtures.menu.v2.ValidMenuState.*;

import java.util.function.Consumer;

import domain.pos.menu.entity.v2.state.MenuInfoState;

public class MenuInfoStateFixture implements MenuInfoState {
	private String name;
	private Integer price;
	private String description;
	private String imageUrl;

	private MenuInfoStateFixture(String name, Integer price, String description,
		String imageUrl) {
		this.name = name;
		this.price = price;
		this.description = description;
		this.imageUrl = imageUrl;
	}

	public static MenuInfoStateFixture VALID_STATE() {
		return new MenuInfoStateFixture(
			VALID_NAME_1,
			VALID_PRICE_1,
			VALID_DESCRIPTION_1,
			VALID_IMAGE_URL_1
		);
	}

	public static MenuInfoStateFixture ANOTHER_VALID_STATE() {
		return new MenuInfoStateFixture(
			VALID_NAME_2,
			VALID_PRICE_2,
			VALID_DESCRIPTION_2,
			VALID_IMAGE_URL_2
		);
	}

	public static MenuInfoStateFixture custom(Consumer<MenuInfoStateFixture> mutator) {
		var fixture = VALID_STATE();
		mutator.accept(fixture);
		return fixture;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Integer getPrice() {
		return this.price;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public String getImageUrl() {
		return this.imageUrl;
	}

	public void customName(String name) {
		this.name = name;
	}

	public void customPrice(Integer price) {
		this.price = price;
	}

	public void customDescription(String description) {
		this.description = description;
	}

	public void customImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
}
