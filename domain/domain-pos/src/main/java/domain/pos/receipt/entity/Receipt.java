package domain.pos.receipt.entity;

import java.util.ArrayList;
import java.util.List;

import domain.pos.order.entity.Order;
import domain.pos.order.entity.vo.OrderStatus;
import domain.pos.sale.entity.Sale;
import domain.pos.table.entity.Table;
import lombok.Getter;

@Getter
public class Receipt {
	private final ReceiptInfo receiptInfo;
	private final Sale sale;
	private final Table table;
	private final List<Order> orders = new ArrayList<>();

	private Receipt(ReceiptInfo receiptInfo, Sale sale, Table table) {
		this.receiptInfo = receiptInfo;
		this.sale = sale;
		this.table = table;
	}

	public static Receipt of(
		final ReceiptInfo receiptInfo,
		final Sale sale,
		final Table table) {
		return new Receipt(
			receiptInfo,
			sale,
			table
		);
	}

	public void filterCompletedOrders() {
		this.orders.removeIf(order -> order.getOrderStatus() != OrderStatus.COMPLETED);
	}
}
