package domain.pos.receipt.entity.v2.dto;

import java.util.UUID;

import domain.pos.order.entity.vo.OrderStatus;
import lombok.Value;

@Value
public class ReceiptOrderStatus {
	UUID receiptId;
	OrderStatus orderStatus;
}
