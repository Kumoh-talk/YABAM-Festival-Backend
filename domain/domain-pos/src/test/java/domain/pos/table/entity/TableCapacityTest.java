package domain.pos.table.entity;

import static domain.pos.table.entity.TableCapacity.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.exception.VoException;

class TableCapacityTest {

	@Test
	void createTest() {
		var tableCapacity = of(4);

		assertThat(tableCapacity).isEqualTo(of(4));
	}

	@Test
	void createFailTest() {
		assertThatThrownBy(() ->
			of(null)
		).isInstanceOf(VoException.class);

		assertThatThrownBy(() ->
			of(-1)
		).isInstanceOf(VoException.class);
	}

}
