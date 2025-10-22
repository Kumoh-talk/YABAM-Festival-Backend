package domain.pos.receipt.entity.v2.dto;

import domain.pos.sale.entity.Sale;
import domain.pos.table.entity.Table;

public record CreateValidationResult(
	Sale sale,
	Table table
) {
}
