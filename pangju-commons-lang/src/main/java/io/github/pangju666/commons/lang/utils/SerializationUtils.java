package io.github.pangju666.commons.lang.utils;

import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.util.Objects;

/**
 * 从Spring Framework 复制过来的
 *
 * @see org.springframework.util.SerializationUtils
 */
public class SerializationUtils {
	protected SerializationUtils() {
	}

	public static byte[] serialize(final Object object) {
		if (Objects.isNull(object)) {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
		try (ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream)) {
			oos.writeObject(object);
			oos.flush();
		} catch (IOException ex) {
			throw new IllegalArgumentException("序列化对象失败，类型: " + object.getClass(), ex);
		}
		return byteArrayOutputStream.toByteArray();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T clone(final T object) {
		byte[] bytes = SerializationUtils.serialize(object);
		if (Objects.isNull(bytes)) {
			return null;
		}
		try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
			return (T) ois.readObject();
		} catch (IOException ex) {
			throw new IllegalArgumentException("反序列化对象失败", ex);
		} catch (ClassNotFoundException ex) {
			throw new IllegalStateException("无法反序列化的对象类型", ex);
		}
	}
}
