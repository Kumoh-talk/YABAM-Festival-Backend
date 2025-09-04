package domain.pos.table.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TableInfoRequest(
	@NotNull
	Integer tableNumber,
	@NotNull
	TablePoint tablePoint,
	@Min(1)
	@NotNull
	Integer tableCapacity) {
}
