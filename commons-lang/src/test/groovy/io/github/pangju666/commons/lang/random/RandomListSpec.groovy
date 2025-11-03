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

		when:
		randomList.randomIntegerList(-1)

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
