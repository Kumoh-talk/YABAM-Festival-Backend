package com.uuid;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class UuidUtilsTest {

	@Test
	void randomV7() {
		// when
		UUID uuid = UuidUtils.randomV7();

		// then
		assertEquals(36, uuid.toString().length());
		assertEquals(7, uuid.version());
	}

	@Test
	void uuidV7_순서보장_테스트() throws InterruptedException {
		UUID uuid1 = UuidUtils.randomV7();
		Thread.sleep(1); // 최소한의 시간 간격
		UUID uuid2 = UuidUtils.randomV7();

		int comparison = uuid1.toString().compareTo(uuid2.toString());

		assertTrue(comparison < 0, "uuid1 should be lexically less than uuid2");
	}

	@Test
	void uuid속도_테스트() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 1000000000; i++) {
			UuidUtils.randomV7();
		}
		long endTime = System.currentTimeMillis();
		System.out.println("UUID 생성 시간: " + (endTime - startTime) + "ms");
		assertTrue((endTime - startTime) < 100000, "UUID generation took too long");
	}
}
