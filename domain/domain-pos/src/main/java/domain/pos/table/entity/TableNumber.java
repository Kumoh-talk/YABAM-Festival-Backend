package domain.pos.table.entity;

import com.exception.VoException;

public record TableNumber(Integer value) {
	public static TableNumber of(Integer value) {
		if (value == null || value <= 0) {
			throw new VoException("테이블 번호는 양수이어야 합니다.");
		}
		return new TableNumber(value);
	}
}
