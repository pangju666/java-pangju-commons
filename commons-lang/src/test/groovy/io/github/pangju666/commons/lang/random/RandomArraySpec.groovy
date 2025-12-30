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

class RandomArraySpec extends Specification {
	def "测试单例实例获取"() {
		when: "获取不同安全级别的实例"
		def insecure = RandomArray.insecure()
		def secure = RandomArray.secure()
		def secureStrong = RandomArray.secureStrong()

		then: "验证实例非空且类型正确"
		insecure != null
		secure != null
		secureStrong != null
		insecure.class == RandomArray
		secure.class == RandomArray
		secureStrong.class == RandomArray
	}

	@Unroll
	def "测试randomBooleanArray方法 - 长度: #length"() {
		given: "准备随机数组生成器"
		def randomArray = RandomArray.insecure()

		when: "生成布尔数组"
		def result = randomArray.randomBooleanArray(length)

		then: "验证数组长度和元素类型"
		result.length == length
		result.every { it instanceof Boolean }

		where:
		length << [1, 5, 10]
	}

	@Unroll
	def "测试整数数组生成 (randomIntArray) - 范围: #start-#end, 长度: #length"() {
		given:
		def randomArray = RandomArray.insecure()

		when:
		def result = randomArray.randomIntArray(start, end, length)

		then:
		result.length == length
		result.every { it >= start && it < end }

		where:
		start | end | length
		0     | 10  | 5
		5     | 20  | 8
		0     | 1   | 1
	}

	def "测试全范围整数数组生成 (randomIntArray)"() {
		given:
		def randomArray = RandomArray.insecure()

		when:
		def result = randomArray.randomIntArray(10)

		then:
		result.length == 10
		// 全范围生成，只要不报错即可，值可以是任意int
	}

	@Unroll
	def "测试唯一整数数组生成 (randomUniqueIntArray) - 范围: #start-#end, 长度: #length"() {
		given:
		def randomArray = RandomArray.insecure()

		when:
		def result = randomArray.randomUniqueIntArray(start, end, length)

		then:
		result.length == length
		result.every { it >= start && it < end }
		result.toSet().size() == length

		where:
		start | end | length
		0     | 10  | 5
		0     | 5   | 5
		10    | 20  | 8
	}

	def "测试全范围唯一整数数组生成 (randomUniqueIntArray)"() {
		given:
		def randomArray = RandomArray.insecure()

		when:
		def result = randomArray.randomUniqueIntArray(10)

		then:
		result.length == 10
		result.toSet().size() == 10
	}

	@Unroll
	def "测试长整数数组生成 (randomLongArray) - 范围: #start-#end, 长度: #length"() {
		given:
		def randomArray = RandomArray.insecure()

		when:
		def result = randomArray.randomLongArray(start, end, length)

		then:
		result.length == length
		result.every { it >= start && it < end }

		where:
		start | end  | length
		0L    | 10L  | 5
		100L  | 200L | 10
	}

	def "测试全范围长整数数组生成 (randomLongArray)"() {
		given:
		def randomArray = RandomArray.insecure()

		when:
		def result = randomArray.randomLongArray(10)

		then:
		result.length == 10
	}

	@Unroll
	def "测试唯一长整数数组生成 (randomUniqueLongArray) - 范围: #start-#end, 长度: #length"() {
		given:
		def randomArray = RandomArray.insecure()

		when:
		def result = randomArray.randomUniqueLongArray(start, end, length)

		then:
		result.length == length
		result.every { it >= start && it < end }
		result.toSet().size() == length

		where:
		start | end | length
		0L    | 10L | 5
		0L    | 5L  | 5
	}

	def "测试全范围唯一长整数数组生成 (randomUniqueLongArray)"() {
		given:
		def randomArray = RandomArray.insecure()

		when:
		def result = randomArray.randomUniqueLongArray(10)

		then:
		result.length == 10
		result.toSet().size() == 10
	}

	@Unroll
	def "测试浮点数数组生成 (randomFloatArray) - 范围: #start-#end, 长度: #length"() {
		given:
		def randomArray = RandomArray.insecure()

		when:
		def result = randomArray.randomFloatArray(start, end, length)

		then:
		result.length == length
		result.every { it >= start && it < end }

		where:
		start | end  | length
		0.0f  | 1.0f | 5
		1.5f  | 2.5f | 3
	}

	def "测试全范围浮点数数组生成 (randomFloatArray)"() {
		given:
		def randomArray = RandomArray.insecure()

		when:
		def result = randomArray.randomFloatArray(10)

		then:
		result.length == 10
	}

	@Unroll
	def "测试双精度数组生成 (randomDoubleArray) - 范围: #start-#end, 长度: #length"() {
		given:
		def randomArray = RandomArray.insecure()

		when:
		def result = randomArray.randomDoubleArray(start, end, length)

		then:
		result.length == length
		result.every { it >= start && it < end }

		where:
		start | end  | length
		0.0d  | 1.0d | 5
		1.5d  | 2.5d | 3
	}

	def "测试全范围双精度数组生成 (randomDoubleArray)"() {
		given:
		def randomArray = RandomArray.insecure()

		when:
		def result = randomArray.randomDoubleArray(10)

		then:
		result.length == 10
	}

	@Unroll
	def "测试唯一双精度数组生成 (randomUniqueDoubleArray) - 范围: #start-#end, 长度: #length"() {
		given:
		def randomArray = RandomArray.insecure()

		when:
		def result = randomArray.randomUniqueDoubleArray(start, end, length)

		then:
		result.length == length
		result.every { it >= start && it < end }
		result.toSet().size() == length

		where:
		start | end  | length
		0.0d  | 1.0d | 5
		0.0d  | 0.1d | 5 // 小范围测试
	}

	def "测试全范围唯一双精度数组生成 (randomUniqueDoubleArray)"() {
		given:
		def randomArray = RandomArray.insecure()

		when:
		def result = randomArray.randomUniqueDoubleArray(10)

		then:
		result.length == 10
		result.toSet().size() == 10
	}

	def "测试异常情况 - 非法长度"() {
		given:
		def randomArray = RandomArray.insecure()

		when: "长度为0"
		randomArray.randomIntArray(0)
		then:
		thrown(IllegalArgumentException)

		when: "长度为负数"
		randomArray.randomLongArray(-1)
		then:
		thrown(IllegalArgumentException)
	}

	def "测试异常情况 - 范围不足 (Unique生成)"() {
		given:
		def randomArray = RandomArray.insecure()

		when: "整数范围不足以生成唯一数组"
		randomArray.randomUniqueIntArray(0, 5, 6)
		then:
		thrown(IllegalArgumentException)

		when: "长整数范围不足以生成唯一数组"
		randomArray.randomUniqueLongArray(0L, 5L, 6)
		then:
		thrown(IllegalArgumentException)
	}

	def "测试异常情况 - 非法范围 (start > end 或 start < 0)"() {
		given:
		def randomArray = RandomArray.insecure()

		when: "start > end"
		randomArray.randomIntArray(10, 5, 5)
		then:
		thrown(IllegalArgumentException)

		when: "start < 0"
		randomArray.randomIntArray(-1, 5, 5)
		then:
		thrown(IllegalArgumentException)
	}
}
