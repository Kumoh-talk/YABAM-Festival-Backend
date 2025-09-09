package domain.pos.table.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.exception.VoException;

class TableNumberTest {
	@Test
	void createTest() {
		var tableNumber = TableNumber.of(1);

		assertThat(tableNumber).isEqualTo(new TableNumber(1));
	}

	@Test
	void createFailTest() {
		assertThatThrownBy(() ->
			TableNumber.of(null)
		).isInstanceOf(VoException.class);

		assertThatThrownBy(() ->
			TableNumber.of(0)
		).isInstanceOf(VoException.class);
	}

}
