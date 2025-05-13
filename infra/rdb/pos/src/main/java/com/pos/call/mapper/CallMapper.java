package com.pos.call.mapper;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.pos.call.entity.CallEntity;
import com.pos.receipt.entity.ReceiptEntity;
import com.pos.sale.entity.SaleEntity;

import domain.pos.call.entity.Call;
import domain.pos.call.entity.CallMessage;
import domain.pos.call.entity.TableCallInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CallMapper {

	public static CallEntity toCallEntity(Long receiptId, Long saleId, CallMessage callMessage) {
		ReceiptEntity receiptEntity = ReceiptEntity.from(receiptId);
		SaleEntity saleEntity = SaleEntity.from(saleId);
		return CallEntity
			.of(receiptEntity, saleEntity, callMessage);
	}

	public static Slice<Call> toCallSlice(Slice<CallEntity> nonCompleteCalls) {
		return new SliceImpl<>(
			nonCompleteCalls.map(CallMapper::toCall).toList(),
			PageRequest.of(0, nonCompleteCalls.getSize()),
			nonCompleteCalls.hasNext());
	}

	public static Call toCall(CallEntity callEntity) {
		return Call.of(
			callEntity.getSale().getId(),
			callEntity.getId(),
			TableCallInfo.of(
				callEntity.getReceipt().getTable().getId(),
				callEntity.getReceipt().getTable().getTableNumber().getTableNumber(),
				callEntity.getReceipt().getId()
			),
			CallMessage.of(
				callEntity.getMessage(),
				callEntity.getIsCompleted()
			),
			callEntity.getCreatedAt()
		);
	}

}
