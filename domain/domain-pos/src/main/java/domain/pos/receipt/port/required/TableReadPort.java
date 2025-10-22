package domain.pos.receipt.port.required;

public interface TableReadPort {
	boolean readIsActiveBy(Long storeId, String tableId);
}
