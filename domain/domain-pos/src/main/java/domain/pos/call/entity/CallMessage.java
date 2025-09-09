package domain.pos.call.entity;

import static com.exception.ErrorCode.*;

import com.exception.ServiceException;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class CallMessage {
	private String message;
	private Boolean isComplete;

	private CallMessage(String message, Boolean isComplete) {
		this.message = message;
		this.isComplete = isComplete;
	}

	public static CallMessage of(String message, Boolean isComplete) {
		return new CallMessage(message, isComplete);
	}

	public static CallMessage create(String message) {
		return new CallMessage(message, false);
	}

	public void complete() {
		if (this.isComplete) {
			throw new ServiceException(ALREADY_COMPLETED_CALL);
		}

		this.isComplete = true;
	}
}
