package domain.pos.call.entity;

import static java.time.LocalDateTime.*;
import static java.util.Objects.*;

import java.time.LocalDateTime;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = {"id"})
public class Call {
	private Long id;
	private Long saleId;
	private Long receiptId;
	private TableCallInfo tableCallInfo;
	private CallMessage callMessage;
	private LocalDateTime createdAt;

	private Call(Long saleId, Long id, TableCallInfo tableCallInfo, CallMessage callMessage,
		LocalDateTime createdAt) {
		this.saleId = saleId;
		this.id = id;
		this.tableCallInfo = tableCallInfo;
		this.callMessage = callMessage;
		this.createdAt = createdAt;
	}

	private Call() {
	}

	public static Call of(Long saleId, Long callId, TableCallInfo tableCallInfo, CallMessage callMessage,
		LocalDateTime createdAt) {
		return new Call(saleId, callId, tableCallInfo, callMessage, createdAt);
	}

	public static Call create(final Long saleId, final Long receiptId, final String message) {
		var call = new Call();
		call.saleId = requireNonNull(saleId);
		call.receiptId = requireNonNull(receiptId);
		call.callMessage = CallMessage.create(requireNonNull(message));
		call.createdAt = now();

		return call;
	}

	public void complete() {
		this.callMessage.complete();
	}
}
