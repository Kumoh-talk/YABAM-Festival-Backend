package domain.pos.call.entity;

import lombok.Getter;

@Getter
public class Call {
	private final Long storeId;
	private final Long callId;
	private final TableCallInfo tableCallInfo;
	private final CallMessage callMessage;

	private Call(Long storeId, Long callId, TableCallInfo tableCallInfo, CallMessage callMessage) {
		this.storeId = storeId;
		this.callId = callId;
		this.tableCallInfo = tableCallInfo;
		this.callMessage = callMessage;
	}

	public static Call of(Long storeId, Long callId, TableCallInfo tableCallInfo, CallMessage callMessage) {
		return new Call(storeId, callId, tableCallInfo, callMessage);
	}
}
