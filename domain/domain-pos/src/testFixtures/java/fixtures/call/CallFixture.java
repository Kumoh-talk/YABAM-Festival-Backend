package fixtures.call;

import java.time.LocalDateTime;
import java.util.UUID;

import domain.pos.call.entity.Call;
import domain.pos.call.entity.CallMessage;
import domain.pos.call.entity.TableCallInfo;
import domain.pos.receipt.entity.Receipt;
import domain.pos.sale.entity.Sale;
import domain.pos.table.entity.Table;

public class CallFixture {

	private static final Long CALL_ID = 1L;
	private static final Long SALE_ID = 100L;
	private static final UUID TABLE_ID = UUID.randomUUID();
	private static final Integer TABLE_NUMBER = 5;
	private static final UUID RECEIPT_ID = UUID.randomUUID();
	private static final String MESSAGE = "직원 호출 요청입니다.";
	private static final Boolean IS_COMPLETE = false;
	private static final LocalDateTime CREATED_AT = LocalDateTime.of(2025, 5, 1, 12, 0);

	public static Call CREATE_CALL() {
		var saleId = 1L;
		var receiptId = UUID.randomUUID();
		var message = "call message";

		return Call.create(saleId, receiptId, message);
	}

	public static Call GENERAL_CALL() {
		return Call.of(
			SALE_ID,
			CALL_ID,
			GENERAL_TABLE_CALL_INFO(),
			GENERAL_CALL_MESSAGE(),
			CREATED_AT
		);
	}

	public static Call CUSTOM_GENERAL_CALL(Sale sale, Table table, Receipt receipt) {
		return Call.of(
			sale.getId(),
			CALL_ID,
			TableCallInfo.of(
				table.getId(),
				table.getTableNumber().value(),
				receipt.getReceiptInfo().getReceiptId()
			),
			CallMessage.of(
				MESSAGE,
				IS_COMPLETE
			),
			CREATED_AT
		);
	}

	public static TableCallInfo GENERAL_TABLE_CALL_INFO() {
		return TableCallInfo.of(
			TABLE_ID,
			TABLE_NUMBER,
			RECEIPT_ID
		);
	}

	public static CallMessage GENERAL_CALL_MESSAGE() {
		return CallMessage.of(
			MESSAGE,
			IS_COMPLETE
		);
	}
}
