/*
 *   Copyright 2025 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.pangju666.commons.lang.utils

import spock.lang.Specification
import spock.lang.Unroll

class ArrayUtilsSpec extends Specification {

	// -----------------------------------------------------------------------
	// Generic Array Partition Tests
	// -----------------------------------------------------------------------

	@Unroll
	def "测试泛型数组分割 (partition) - 输入: #array, 大小: #size -> 结果: #expected"() {
		expect:
		ArrayUtils.partition(array as String[], size) == expected

		where:
		array                | size || expected
		null                 | 2    || []
		[]                   | 2    || []
		["a", "b", "c"]      | 2    || [["a", "b"] as String[], ["c"] as String[]]
		["a", "b", "c", "d"] | 2    || [["a", "b"] as String[], ["c", "d"] as String[]]
		["a", "b", "c"]      | 1    || [["a"] as String[], ["b"] as String[], ["c"] as String[]]
		["a", "b", "c"]      | 3    || [["a", "b", "c"] as String[]]
		["a", "b", "c"]      | 4    || [["a", "b", "c"] as String[]]
		[null, "", "text"]   | 2    || [[null, ""] as String[], ["text"] as String[]]
		["text", null, ""]   | 2    || [["text", null] as String[], [""] as String[]]
	}

	@Unroll
	def "测试泛型数组分割异常情况 - 输入: #array, 大小: #size"() {
		expect:
		ArrayUtils.partition(array as String[], size) == []

		where:
		array      | size
		["a", "b"] | 0
		["a", "b"] | -1
		null       | 0
		[]         | -1
	}

	// -----------------------------------------------------------------------
	// Primitive Array Partition Tests
	// -----------------------------------------------------------------------

	@Unroll
	def "测试int数组分割 (partition) - 输入: #array, 大小: #size"() {
		expect:
		def result = ArrayUtils.partition(array as int[], size)
		result.size() == expectedSize
		if (expectedSize > 0) {
			result[0] == firstPartition as int[]
		}

		where:
		array        | size || expectedSize | firstPartition
		null         | 2    || 0            | []
		[]           | 2    || 0            | []
		[1, 2, 3]    | 2    || 2            | [1, 2]
		[1, 2, 3, 4] | 2    || 2            | [1, 2]
		[1, 2, 3]    | 1    || 3            | [1]
		[1, 2, 3]    | 3    || 1            | [1, 2, 3]
		[1, 2, 3]    | 4    || 1            | [1, 2, 3]
	}

	@Unroll
	def "测试long数组分割 (partition) - 输入: #array, 大小: #size"() {
		expect:
		def result = ArrayUtils.partition(array as long[], size)
		result.size() == expectedSize
		if (expectedSize > 0) {
			result[0] == firstPartition as long[]
		}

		where:
		array        | size || expectedSize | firstPartition
		null         | 2    || 0            | []
		[]           | 2    || 0            | []
		[1L, 2L, 3L] | 2    || 2            | [1L, 2L]
	}

	@Unroll
	def "测试float数组分割 (partition) - 输入: #array, 大小: #size"() {
		expect:
		def result = ArrayUtils.partition(array as float[], size)
		result.size() == expectedSize
		if (expectedSize > 0) {
			result[0] == firstPartition as float[]
		}

		where:
		array        | size || expectedSize | firstPartition
		null         | 2    || 0            | []
		[]           | 2    || 0            | []
		[1f, 2f, 3f] | 2    || 2            | [1f, 2f]
	}

	@Unroll
	def "测试double数组分割 (partition) - 输入: #array, 大小: #size"() {
		expect:
		def result = ArrayUtils.partition(array as double[], size)
		result.size() == expectedSize
		if (expectedSize > 0) {
			result[0] == firstPartition as double[]
		}

		where:
		array        | size || expectedSize | firstPartition
		null         | 2    || 0            | []
		[]           | 2    || 0            | []
		[1d, 2d, 3d] | 2    || 2            | [1d, 2d]
	}

	@Unroll
	def "测试short数组分割 (partition) - 输入: #array, 大小: #size"() {
		expect:
		def result = ArrayUtils.partition(array as short[], size)
		result.size() == expectedSize
		if (expectedSize > 0) {
			result[0] == firstPartition as short[]
		}

		where:
		array     | size || expectedSize | firstPartition
		null      | 2    || 0            | []
		[]        | 2    || 0            | []
		[1, 2, 3] | 2    || 2            | [1, 2]
	}

	@Unroll
	def "测试byte数组分割 (partition) - 输入: #array, 大小: #size"() {
		expect:
		def result = ArrayUtils.partition(array as byte[], size)
		result.size() == expectedSize
		if (expectedSize > 0) {
			result[0] == firstPartition as byte[]
		}

		where:
		array     | size || expectedSize | firstPartition
		null      | 2    || 0            | []
		[]        | 2    || 0            | []
		[1, 2, 3] | 2    || 2            | [1, 2]
	}

	@Unroll
	def "测试char数组分割 (partition) - 输入: #array, 大小: #size"() {
		expect:
		def result = ArrayUtils.partition(array as char[], size)
		result.size() == expectedSize
		if (expectedSize > 0) {
			result[0] == firstPartition as char[]
		}

		where:
		array           | size || expectedSize | firstPartition
		null            | 2    || 0            | []
		[]              | 2    || 0            | []
		['a', 'b', 'c'] | 2    || 2            | ['a', 'b']
	}

	@Unroll
	def "测试boolean数组分割 (partition) - 输入: #array, 大小: #size"() {
		expect:
		def result = ArrayUtils.partition(array as boolean[], size)
		result.size() == expectedSize
		if (expectedSize > 0) {
			result[0] == firstPartition as boolean[]
		}

		where:
		array               | size || expectedSize | firstPartition
		null                | 2    || 0            | []
		[]                  | 2    || 0            | []
		[false, true, true] | 2    || 2            | [false, true]
	}
}
