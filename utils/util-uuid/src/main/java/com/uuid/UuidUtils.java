package com.uuid;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class UuidUtils {
	private UuidUtils() {
	}

	public static UUID randomV7() {
		byte[] value = randomBytes();
		ByteBuffer buf = ByteBuffer.wrap(value);
		long high = buf.getLong();
		long low = buf.getLong();
		return new UUID(high, low);
	}

	public static byte[] randomBytes() {
		byte[] value = new byte[16];
		ThreadLocalRandom.current().nextBytes(value);

		ByteBuffer ts = ByteBuffer.allocate(Long.BYTES)
			.putLong(System.currentTimeMillis());
		System.arraycopy(ts.array(), 2, value, 0, 6);

		value[6] = (byte)((value[6] & 0x0F) | 0x70);
		value[8] = (byte)((value[8] & 0x3F) | 0x80);

		return value;
	}
}
