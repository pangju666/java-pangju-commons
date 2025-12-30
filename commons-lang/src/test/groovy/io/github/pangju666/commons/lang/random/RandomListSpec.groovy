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

package io.github.pangju666.commons.lang.random

import spock.lang.Specification
import spock.lang.Unroll

class RandomListSpec extends Specification {
	// 单例实例测试
	def "测试单例实例初始化"() {
		when:
		def insecure = RandomList.insecure()
		def secure = RandomList.secure()
		def secureStrong = RandomList.secureStrong()

		then:
		insecure != null
		secure != null
		secureStrong != null
	}

	// 布尔列表测试
	@Unroll
	def "测试randomBooleanList方法 - length=#length"() {
		given:
		def randomList = RandomList.insecure()

		when:
		def result = randomList.randomBooleanList(length)

		then:
		result.size() == length
		result.every { it in [true, false] }

		where:
		length << [1, 5, 10]
	}

	// 整数列表测试
	@Unroll
	def "测试randomIntegerList范围生成 - start=#start, end=#end, length=#length"() {
		given:
		def randomList = RandomList.insecure()

		when:
		def result = randomList.randomIntegerList(start, end, length)

		then:
		result.size() == length
		result.every { it >= start && it < end }

		where:
		start | end | length
		0     | 10  | 5
		5     | 20  | 8
	}

	// 唯一性测试
	@Unroll
	def "测试randomUniqueIntegerList唯一性 - start=#start, end=#end, length=#length"() {
		given:
		def randomList = RandomList.insecure()

		when:
		def result = randomList.randomUniqueIntegerList(start, end, length)

		then:
		result.size() == length
		result.toSet().size() == length
		result.every { it >= start && it < end }

		where:
		start | end | length
		1     | 100 | 10
		0     | 50  | 5
	}

	def "测试全范围唯一整数列表 (randomUniqueIntegerList)"() {
		given:
		def randomList = RandomList.insecure()

		when:
		def result = randomList.randomUniqueIntegerList(10)

		then:
		result.size() == 10
		result.toSet().size() == 10
	}

	// 浮点数测试
	@Unroll
	def "测试randomFloatList范围生成 - start=#start, end=#end, length=#length"() {
		given:
		def randomList = RandomList.insecure()

		when:
		def result = randomList.randomFloatList(start, end, length)

		then:
		result.size() == length
		result.every { it >= start && it < end }

		where:
		start | end   | length
		0.0f  | 1.0f  | 5
		5.5f  | 10.5f | 8
	}

	// 异常测试
	def "测试非法参数异常"() {
		given:
		def randomList = RandomList.insecure()

		when: "length 为负数"
		randomList.randomIntegerList(-1)
		then:
		thrown(IllegalArgumentException)

		when: "start > end"
		randomList.randomIntegerList(10, 5, 5)
		then:
		thrown(IllegalArgumentException)

		when: "start < 0"
		randomList.randomIntegerList(-1, 5, 5)
		then:
		thrown(IllegalArgumentException)

		when: "整数范围不足以生成唯一数组"
		randomList.randomUniqueIntegerList(0, 5, 6)
		then:
		thrown(IllegalArgumentException)

		when: "长整数范围不足以生成唯一数组"
		randomList.randomUniqueLongList(0L, 5L, 6)
		then:
		thrown(IllegalArgumentException)
	}

	// 长整型测试
	@Unroll
	def "测试randomLongList范围生成 - start=#start, end=#end, length=#length"() {
		given:
		def randomList = RandomList.insecure()

		when:
		def result = randomList.randomLongList(start, end, length)

		then:
		result.size() == length
		result.every { it >= start && it < end }

		where:
		start | end | length
		0L    | 10L | 5
		5L    | 20L | 8
	}

	def "测试全范围长整数列表 (randomLongList)"() {
		given:
		def randomList = RandomList.insecure()

		when:
		def result = randomList.randomLongList(10)

		then:
		result.size() == 10
	}

	def "测试全范围唯一长整数列表 (randomUniqueLongList)"() {
		given:
		def randomList = RandomList.insecure()

		when:
		def result = randomList.randomUniqueLongList(10)

		then:
		result.size() == 10
		result.toSet().size() == 10
	}

	// 双精度浮点数测试
	@Unroll
	def "测试randomDoubleList范围生成 - start=#start, end=#end, length=#length"() {
		given:
		def randomList = RandomList.insecure()

		when:
		def result = randomList.randomDoubleList(start, end, length)

		then:
		result.size() == length
		result.every { it >= start && it < end }

		where:
		start | end   | length
		0.0d  | 1.0d  | 5
		5.5d  | 10.5d | 8
	}

	def "测试全范围双精度浮点数列表 (randomDoubleList)"() {
		given:
		def randomList = RandomList.insecure()

		when:
		def result = randomList.randomDoubleList(10)

		then:
		result.size() == 10
	}

	// 双精度浮点数唯一性测试
	@Unroll
	def "测试randomUniqueDoubleList唯一性 - start=#start, end=#end, length=#length"() {
		given:
		def randomList = RandomList.insecure()

		when:
		def result = randomList.randomUniqueDoubleList(start, end, length)

		then:
		result.size() == length
		result.toSet().size() == length
		result.every { it >= start && it < end }

		where:
		start | end   | length
		0.0d  | 1.0d  | 5
		5.5d  | 10.5d | 5
	}

	def "测试全范围唯一双精度浮点数列表 (randomUniqueDoubleList)"() {
		given:
		def randomList = RandomList.insecure()

		when:
		def result = randomList.randomUniqueDoubleList(10)

		then:
		result.size() == 10
		result.toSet().size() == 10
	}

	// 浮点数更多测试
	def "测试全范围浮点数列表 (randomFloatList)"() {
		given:
		def randomList = RandomList.insecure()

		when:
		def result = randomList.randomFloatList(10)

		then:
		result.size() == 10
	}

	def "测试全范围唯一浮点数列表 (randomUniqueFloatList)"() {
		given:
		def randomList = RandomList.insecure()

		when:
		def result = randomList.randomUniqueFloatList(10)

		then:
		result.size() == 10
		result.toSet().size() == 10
	}

	@Unroll
	def "测试randomUniqueFloatList唯一性 - start=#start, end=#end, length=#length"() {
		given:
		def randomList = RandomList.insecure()

		when:
		def result = randomList.randomUniqueFloatList(start, end, length)

		then:
		result.size() == length
		result.toSet().size() == length
		result.every { it >= start && it < end }

		where:
		start | end   | length
		0.0f  | 1.0f  | 5
		5.5f  | 10.5f | 5
	}

	// 全范围测试样例
	def "测试全范围随机列表"() {
		given:
		def randomList = RandomList.insecure()

		when:
		def intList = randomList.randomIntegerList(5)
		def doubleList = randomList.randomDoubleList(5)

		then:
		intList.every { it >= 0 && it < Integer.MAX_VALUE }
		doubleList.every { it >= 0d && it < Double.MAX_VALUE }
	}

	// 压力测试样例
	def "测试大数据量场景"() {
		given:
		def randomList = RandomList.insecure()

		when:
		def result = randomList.randomUniqueIntegerList(1, 10000, 5000)

		then:
		result.size() == 5000
		result.toSet().size() == 5000
	}
}
