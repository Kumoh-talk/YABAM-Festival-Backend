package domain.pos.receipt.entity.v2.dto;

import domain.pos.order.entity.vo.OrderStatus;
import lombok.Value;

@Value
public class ReceiptOrderStatus {
	Long receiptId;
	OrderStatus orderStatus;
}
