package domain.global;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AbstractEntityTest {

	class TestEntity extends AbstractEntity {

	}

	@Test
	void equalTest() {
		TestEntity te = new TestEntity();
		TestEntity te2 = new TestEntity();

		ReflectionTestUtils.setField(te, "id", 1L);
		ReflectionTestUtils.setField(te2, "id", 1L);

		assertThat(te).isEqualTo(te2);
		assertThat(te.hashCode()).isEqualTo(te2.hashCode());
	}
}
