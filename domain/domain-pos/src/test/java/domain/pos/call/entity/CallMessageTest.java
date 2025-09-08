package domain.pos.call.entity;

import static com.exception.ErrorCode.*;
import static domain.pos.call.entity.CallMessage.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.exception.ServiceException;

class CallMessageTest {

	@Test
	void createTest() {
		var message = "call message";

		var callMessage = create(message);

		assertThat(callMessage.getMessage()).isEqualTo(message);
		assertThat(callMessage.getIsComplete()).isFalse();
	}

	@Test
	void completeTest() {
		var message = "call message";

		var callMessage = create(message);

		callMessage.complete();

		assertThat(callMessage.getIsComplete()).isTrue();
	}

	@Test
	void completeFailTest() {
		var message = "call message";

		var callMessage = create(message);

		callMessage.complete();

		assertThatThrownBy(() ->
			callMessage.complete()
		).isInstanceOf(ServiceException.class)
			.hasFieldOrPropertyWithValue("errorCode", ALREADY_COMPLETED_CALL);
	}
}
