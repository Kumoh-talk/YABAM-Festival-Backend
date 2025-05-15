package domain.pos.call.entity;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class Call {
	private final Long saleId;
	private final Long callId;
	private final TableCallInfo tableCallInfo;
	private final CallMessage callMessage;
	private final LocalDateTime createdAt;

	private Call(Long saleId, Long callId, TableCallInfo tableCallInfo, CallMessage callMessage,
		LocalDateTime createdAt) {
		this.saleId = saleId;
		this.callId = callId;
		this.tableCallInfo = tableCallInfo;
		this.callMessage = callMessage;
		this.createdAt = createdAt;
	}

	public static Call of(Long saleId, Long callId, TableCallInfo tableCallInfo, CallMessage callMessage,
		LocalDateTime createdAt) {
		return new Call(saleId, callId, tableCallInfo, callMessage, createdAt);
	}
}
