/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.pangju666.commons.lang.utils;

import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Objects;

/**
 * 使用静态实用程序进行序列化和反序列化
 <a href="https://docs.oracle.com/en/java/javase/17/docs/specs/serialization/"
 * target="_blank">Java对象序列化</a>。
 *
 <p><strong>WARNING</strong>: 应谨慎使用这些实用程序。请参阅
 * <a href="https://www.oracle.com/java/technologies/javase/seccodeguide.html#8"
 * target="_blank">Java编程语言安全编码指南</a>。
 * 以了解详细信息。
 *
 * <p>
 *     代码来源于：org.springframework.util.SerializationUtils
 * </p>
 *
 * @author Dave Syer
 * @author Loïc Ledoyen
 * @author Sam Brannen
 * @since 1.0.0
 */
public class SerializationUtils {
	protected SerializationUtils() {
	}

	/**
	 * 将给定对象序列化为字节数组。
	 *
	 * @param object 要序列化的对象
	 * @return 以可移植方式表示对象的字节数组
	 * @since 1.0.0
	 */
	public static byte[] serialize(@Nullable final Object object) {
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

	/**
	 * 使用 Java Object Serialization 克隆给定对象。
	 * @param object 克隆对象
	 * @param <T> 要克隆对象的类型
	 * @return 给定对象的克隆（深度复制
	 *
	 * @since 1.0.0
	 */
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
