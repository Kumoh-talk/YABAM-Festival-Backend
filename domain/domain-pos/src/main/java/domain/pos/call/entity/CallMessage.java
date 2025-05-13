package domain.pos.call.entity;

import lombok.Getter;

@Getter
public class CallMessage {
	private final String message;
	private final Boolean isComplete;

	private CallMessage(String message, Boolean isComplete) {
		this.message = message;
		this.isComplete = isComplete;
	}

	public static CallMessage of(String message, Boolean isComplete) {
		return new CallMessage(message, isComplete);
	}
}
