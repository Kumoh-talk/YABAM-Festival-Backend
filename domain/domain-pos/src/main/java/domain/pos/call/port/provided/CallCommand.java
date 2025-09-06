package domain.pos.call.port.provided;

import java.util.UUID;

import com.vo.UserPassport;

import domain.pos.call.entity.Call;

public interface CallCommand {
	Call createCall(UUID receiptId, String message);

	Call completeCall(UserPassport passport, Long callId);
}
