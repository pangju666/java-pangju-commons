package io.github.pangju666.commons.lang.utils;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class UUIDUtils {
	protected UUIDUtils() {
	}

	public static UUID randomUUID() {
		return UUID.randomUUID();
	}

	public static UUID fastUUID() {
		return generateUUID(ThreadLocalRandom.current());
	}

	public static UUID generateUUID(final Random random) {
		byte[] randomBytes = new byte[16];
		random.nextBytes(randomBytes);
		randomBytes[6] &= 0x0f;  /* clear version        */
		randomBytes[6] |= 0x40;  /* set to version 4     */
		randomBytes[8] &= 0x3f;  /* clear variant        */
		randomBytes[8] |= 0x80;  /* set to IETF variant  */

		long msb = 0;
		long lsb = 0;
		for (int i = 0; i < 8; i++)
			msb = (msb << 8) | (randomBytes[i] & 0xff);
		for (int i = 8; i < 16; i++)
			lsb = (lsb << 8) | (randomBytes[i] & 0xff);

		return new UUID(msb, lsb);
	}

	public static String toSimpleString(final UUID uuid) {
		return uuid.toString().replace("-", "");
	}
}
