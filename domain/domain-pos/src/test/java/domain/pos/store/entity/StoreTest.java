package domain.pos.store.entity;

import static com.exception.ErrorCode.*;
import static fixtures.member.UserFixture.*;
import static fixtures.store.StoreFixture.*;
import static fixtures.store.StoreInfoFixture.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.exception.ServiceException;

class StoreTest {

	@Test
	void createTest() {
		var passport = OWNER_USER_PASSPORT();
		var storeInfo = GENERAL_STORE_INFO();

		var store = Store.create(passport, storeInfo);

		assertThat(store.getStoreInfo()).isEqualTo(storeInfo);
		assertThat(store.getOwnerPassport().getUserId()).isEqualTo(OWNER_USER_PASSPORT().getUserId());
	}

	@Test
	void createFailTest() {
		var passport = GENERAL_USER_PASSPORT();
		var storeInfo = GENERAL_STORE_INFO();

		assertThatThrownBy(() ->
			Store.create(passport, storeInfo)
		).isInstanceOf(ServiceException.class)
			.hasFieldOrPropertyWithValue("errorCode", NOT_OWNER_STORE_CREATE);
	}

	@Test
	void updateStoreTest() {
		var store = STORE_FIXTURE();
		var passport = store.getOwnerPassport();
		var newStoreInfo = CHANGED_GENERAL_STORE_INFO();

		store.update(passport, newStoreInfo);

		assertThat(store.getStoreInfo()).isEqualTo(newStoreInfo);
	}

	@Test
	void updateFailTest() {
		var store = STORE_FIXTURE();

		var newStoreInfo = CHANGED_GENERAL_STORE_INFO();

		assertThatThrownBy(() ->
			store.update(DIFF_OWNER_PASSPORT(), newStoreInfo)
		).isInstanceOf(ServiceException.class)
			.hasFieldOrPropertyWithValue("errorCode", NOT_EQUAL_STORE_OWNER);
	}

}
