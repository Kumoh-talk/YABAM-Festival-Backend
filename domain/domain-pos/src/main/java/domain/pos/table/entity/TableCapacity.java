package domain.pos.table.entity;

import com.exception.VoException;

public record TableCapacity(Integer value) {
	public static TableCapacity of(Integer value) {
		if (value == null || value < 0) {
			throw new VoException("Table 수용 인원은 0 이상이어야 합니다.");
		}
		return new TableCapacity(value);
	}
}
