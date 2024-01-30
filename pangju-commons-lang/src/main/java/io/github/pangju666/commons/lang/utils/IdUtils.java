package io.github.pangju666.commons.lang.utils;

import io.github.pangju666.commons.lang.id.NanoId;
import org.bson.types.ObjectId;

import java.util.UUID;

public class IdUtils {
	protected IdUtils() {
	}

	public static String randomUUID() {
		return uuid(UUIDUtils.randomUUID());
	}

	public static String simpleRandomUUID() {
		return simpleUUID(UUIDUtils.randomUUID());
	}

	public static String secureRandomUUID() {
		return uuid(UUIDUtils.secureRandomUUID());
	}

	public static String simpleSecureRandomUUID() {
		return simpleUUID(UUIDUtils.secureRandomUUID());
	}

	public static String threadLocalRandomUUID() {
		return uuid(UUIDUtils.threadLocalRandomUUID());
	}

	public static String simpleThreadLocalRandomUUID() {
		return simpleUUID(UUIDUtils.threadLocalRandomUUID());
	}

	public static String uuid(final UUID uuid) {
		return uuid.toString();
	}

	public static String simpleUUID(final UUID uuid) {
		return UUIDUtils.toSimpleString(uuid);
	}

	public static String objectId() {
		return ObjectId.get().toHexString();
	}

	public static String nanoId() {
		return NanoId.randomNanoId();
	}

	public static String nanoId(int size) {
		return NanoId.randomNanoId(size);
	}
}
