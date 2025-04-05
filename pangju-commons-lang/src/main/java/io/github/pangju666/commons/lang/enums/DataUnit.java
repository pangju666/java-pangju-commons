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

package io.github.pangju666.commons.lang.enums;

import io.github.pangju666.commons.lang.model.DataSize;

/**
 * 一套标准的 {@link DataSize} 单位。
 *
 * <p> 该类中使用的单位前缀是
 * <a href="https://en.wikipedia.org/wiki/Binary_prefix">二进制前缀</a> * 表示乘以 2 的幂。
 * 下表显示了该类中定义的
 * 该类中定义的枚举常量和相应的值。
 *
 * <p>
 * <table border="1">
 * <tr><th>常数</th><th>数据大小</th><th>2 的幂次</th><th>大小（字节）</th></tr>
 * <tr><td>{@link #BYTES}</td><td>1B</td><td>2^0</td><td>1</td></tr>。
 * <tr><td>{@link #KILOBYTES}</td><td>1KB</td><td>2^10</td><td>1,024</td></tr>
 * <tr><td>{@link #MEGABYTES}</td><td>1MB</td><td>2^20</td><td>1,048,576</td></tr>。
 * <tr><td>{@link #GIGABYTES}</td><td>1GB</td><td>2^30</td><td>1,073,741,824</td></tr>
 * <tr><td>{@link #TERABYTES}</td><td>1TB</td><td>2^40</td><td>1,099,511,627,776</td></tr>
 * </table>
 *
 * <p>
 *     代码来源于：org.springframework.util.unit.DataUnit
 * </p>
 *
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @see DataSize
 * @since 1.0.0
 */
public enum DataUnit {

	/**
	 * Bytes, represented by suffix {@code B}.
	 */
	BYTES("B", DataSize.ofBytes(1)),

	/**
	 * Kilobytes, represented by suffix {@code KB}.
	 */
	KILOBYTES("KB", DataSize.ofKilobytes(1)),

	/**
	 * Megabytes, represented by suffix {@code MB}.
	 */
	MEGABYTES("MB", DataSize.ofMegabytes(1)),

	/**
	 * Gigabytes, represented by suffix {@code GB}.
	 */
	GIGABYTES("GB", DataSize.ofGigabytes(1)),

	/**
	 * Terabytes, represented by suffix {@code TB}.
	 */
	TERABYTES("TB", DataSize.ofTerabytes(1));


	private final String suffix;

	private final DataSize size;


	DataUnit(String suffix, DataSize size) {
		this.suffix = suffix;
		this.size = size;
	}

	/**
	 * Return the {@link DataUnit} matching the specified {@code suffix}.
	 *
	 * @param suffix one of the standard suffixes
	 * @return the {@link DataUnit} matching the specified {@code suffix}
	 * @throws IllegalArgumentException if the suffix does not match the suffix
	 *                                  of any of this enum's constants
	 */
	public static DataUnit fromSuffix(String suffix) {
		for (DataUnit candidate : values()) {
			if (candidate.suffix.equals(suffix)) {
				return candidate;
			}
		}
		throw new IllegalArgumentException("Unknown data unit suffix '" + suffix + "'");
	}

	public DataSize size() {
		return this.size;
	}
}