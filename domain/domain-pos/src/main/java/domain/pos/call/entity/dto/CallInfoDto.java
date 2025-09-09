package domain.pos.call.entity.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CallInfoDto(
	Long callId,
	String callMessage,
	UUID tableId,
	Integer tableNumber,
	LocalDateTime createdAt
) {
}
