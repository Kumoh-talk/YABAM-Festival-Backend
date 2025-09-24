package com.pos.global.id;

import static com.pos.global.id.IdMapper.*;
import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class IdMapperTest {

	class Dummy {
		private Long id;

		public Long getId() {
			return id;
		}
	}

	class DummyFail {
		private UUID id;

		public UUID getId() {
			return id;
		}
	}

	@Test
	void createTest() {
		var dummy = new Dummy();
		var id = 1L;

		idMapping(dummy, id);

		assertThat(dummy.getId()).isEqualTo(id);
	}

	@Test
	void createFailTest() {
		var dummy = new DummyFail();
		var id = 1L;

		assertThatThrownBy(() -> idMapping(dummy, id))
			.isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> idMapping(dummy, null))
			.isInstanceOf(NullPointerException.class);
	}

	class ChildDummy extends Dummy {
		private String name;

		public String getName() {
			return name;
		}
	}

	@Test
	void createChildTest() {
		var dummy = new ChildDummy();
		var id = 1L;

		idMapping(dummy, id);

		assertThat(dummy.getId()).isEqualTo(id);
	}

}
