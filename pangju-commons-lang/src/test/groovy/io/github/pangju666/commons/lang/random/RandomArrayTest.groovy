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

class RandomArrayTest extends Specification {
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
	def "测试randomIntArray方法 - 范围: #start-#end, 长度: #length"() {
		given: "准备随机数组生成器"
		def randomArray = RandomArray.insecure()

		when: "生成整数数组"
		def result = randomArray.randomIntArray(start, end, length)

		then: "验证数组长度和元素范围"
		result.length == length
		result.every { it >= start && it < end }

		where:
		start | end | length
		0     | 10  | 5
		5     | 20  | 8
	}

	@Unroll
	def "测试randomUniqueIntArray方法 - 范围: #start-#end, 长度: #length"() {
		given: "准备随机数组生成器"
		def randomArray = RandomArray.insecure()

		when: "生成唯一值整数数组"
		def result = randomArray.randomUniqueIntArray(start, end, length)

		then: "验证数组长度、元素范围和唯一性"
		result.length == length
		result.every { it >= start && it < end }
		result.toSet().size() == length

		where:
		start | end | length
		1     | 10  | 5
		5     | 15  | 5
		0     | 100 | 50
	}

	def "测试异常处理 - 非法长度参数"() {
		given: "准备随机数组生成器"
		def randomArray = RandomArray.insecure()

		when: "使用非法长度调用方法"
		randomArray.randomIntArray(-1)

		then: "应该抛出IllegalArgumentException"
		thrown(IllegalArgumentException)
	}

	@Unroll
	def "测试浮点数数组方法 - 类型: #type, 范围: #start-#end, 长度: #length"() {
		given: "准备随机数组生成器"
		def randomArray = RandomArray.insecure()

		when: "生成浮点数数组"
		def result = generateArray(randomArray, type, start, end, length)

		then: "验证数组长度和元素范围"
		result.length == length
		result.every { it >= start && it < end }

		where:
		type     | start | end  | length
		"float"  | 0.0f  | 1.0f | 5
		"double" | 0.0d  | 1.0d | 5
	}

	private static Object generateArray(RandomArray randomArray, String type, def start, def end, int length) {
		switch (type) {
			case "float":
				return randomArray.randomFloatArray(start, end, length)
			case "double":
				return randomArray.randomDoubleArray(start, end, length)
			default:
				throw new IllegalArgumentException("Unsupported type")
		}
	}

	@Unroll
	def "测试长整型数组方法 - 范围: #start-#end, 长度: #length"() {
		given: "准备随机数组生成器"
		def randomArray = RandomArray.insecure()

		when: "生成长整型数组"
		def result = randomArray.randomLongArray(start, end, length)

		then: "验证数组长度和元素范围"
		result.length == length
		result.every { it >= start && it < end }

		where:
		start | end   | length
		0L    | 100L  | 5
		500L  | 1000L | 3
	}

	def "测试全范围随机数组生成"() {
		given: "准备随机数组生成器"
		def randomArray = RandomArray.insecure()

		when: "生成各种类型的全范围数组"
		def intArray = randomArray.randomIntArray(5)
		def longArray = randomArray.randomLongArray(5)
		def doubleArray = randomArray.randomDoubleArray(5)

		then: "验证数组长度和值范围"
		intArray.length == 5
		longArray.length == 5
		doubleArray.length == 5
	}
}
