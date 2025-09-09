package domain.pos.call.entity;

import static fixtures.call.CallFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class CallTest {

	@Test
	void createTest() {
		var saleId = 1L;
		var receiptId = UUID.randomUUID();
		var message = "call message";

		var call = Call.create(saleId, receiptId, message);

		assertThat(call.getCallMessage().getMessage()).isEqualTo(message);
		assertThat(call.getCallMessage().getIsComplete()).isFalse();
		assertThat(call.getSaleId()).isEqualTo(saleId);
		assertThat(call.getReceiptId()).isEqualTo(receiptId);
	}

	@Test
	void completeTest() {
		var call = CREATE_CALL();

		call.complete();
	}

	@Test
	void completeFailTest() {
		var call = CREATE_CALL();

		call.complete();

		assertThatThrownBy(() ->
			call.complete()
		).isInstanceOf(Exception.class);
	}

}
