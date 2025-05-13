package domain.pos.call.entity;

import lombok.Getter;

@Getter
public class Call {
	private final Long saleId;
	private final Long callId;
	private final TableCallInfo tableCallInfo;
	private final CallMessage callMessage;

	private Call(Long saleId, Long callId, TableCallInfo tableCallInfo, CallMessage callMessage) {
		this.saleId = saleId;
		this.callId = callId;
		this.tableCallInfo = tableCallInfo;
		this.callMessage = callMessage;
	}

	public static Call of(Long saleId, Long callId, TableCallInfo tableCallInfo, CallMessage callMessage) {
		return new Call(saleId, callId, tableCallInfo, callMessage);
	}
}
