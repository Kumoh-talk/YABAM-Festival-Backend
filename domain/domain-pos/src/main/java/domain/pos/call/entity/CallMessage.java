package domain.pos.call.entity;

import domain.pos.call.entity.vo.CallCategory;

public class CallMessage {
	private final String message;
	private final CallCategory callCategory;
	private final Boolean isComplete;

	private CallMessage(String message, CallCategory callCategory, Boolean isComplete) {
		this.message = message;
		this.callCategory = callCategory;
		this.isComplete = isComplete;
	}

	public static CallMessage of(String message, CallCategory callCategory, Boolean isComplete) {
		return new CallMessage(message, callCategory, isComplete);
	}
}
