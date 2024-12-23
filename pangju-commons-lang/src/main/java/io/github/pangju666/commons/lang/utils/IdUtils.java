package io.github.pangju666.commons.lang.utils;

import io.github.pangju666.commons.lang.id.NanoId;
import io.github.pangju666.commons.lang.id.SnowflakeIdWorker;
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

	public static String fastUUID() {
		return uuid(UUIDUtils.fastUUID());
	}

	public static String simpleFastUUID() {
		return simpleUUID(UUIDUtils.fastUUID());
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

	public static Long snowflakeId(SnowflakeIdWorker worker) {
		return worker.nextId();
	}
}
