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

package io.github.pangju666.commons.lang.id

import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

import java.security.SecureRandom

@Title("NanoId 单元测试")
class NanoIdSpec extends Specification {

	def "测试默认 randomNanoId 生成"() {
		when:
		String id = NanoId.randomNanoId()

		then:
		id != null
		id.length() == NanoId.DEFAULT_SIZE
		// 验证字符是否都在默认字母表内 (Base64 URL safe)
		id.chars().allMatch { ch ->
			((ch >= '0' && ch <= '9') ||
				(ch >= 'a' && ch <= 'z') ||
				(ch >= 'A' && ch <= 'Z')    ||
				ch == '_'                   || ch == '-')
		}
	}

	@Unroll
	def "测试指定长度 randomNanoId: size=#size"() {
		when:
		String id = NanoId.randomNanoId(size)

		then:
		id.length() == size

		where:
		size << [1, 10, 21, 100]
	}

	def "测试自定义参数 randomNanoId"() {
		given:
		SecureRandom random = new SecureRandom()
		char[] alphabet = "0123456789".toCharArray() // 仅数字
		int size = 10

		when:
		String id = NanoId.randomNanoId(random, alphabet, size)

		then:
		id.length() == size
		id.chars().allMatch { ch -> ch >= '0' && ch <= '9' }
	}

	def "测试参数校验"() {
		when: "size <= 0"
		NanoId.randomNanoId(0)
		then:
		thrown(IllegalArgumentException)

		when: "random 为 null"
		NanoId.randomNanoId(null, "abc".toCharArray(), 10)
		then:
		thrown(NullPointerException)

		when: "alphabet 为空"
		NanoId.randomNanoId(new SecureRandom(), new char[0], 10)
		then:
		thrown(IllegalArgumentException)

		when: "alphabet 过大 (>255)"
		char[] largeAlphabet = new char[256]
		NanoId.randomNanoId(new SecureRandom(), largeAlphabet, 10)
		then:
		thrown(IllegalArgumentException)
	}

	def "测试唯一性 (简单校验)"() {
		when:
		Set<String> ids = new HashSet<>()
		for (int i = 0; i < 1000; i++) {
			ids.add(NanoId.randomNanoId())
		}

		then:
		ids.size() == 1000
	}
}
