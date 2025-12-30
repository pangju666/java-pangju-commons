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

package io.github.pangju666.commons.lang.gson.type


import com.google.gson.GsonBuilder
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("Gson Number TypeAdapters 单元测试")
class GsonNumberTypeAdaptersSpec extends Specification {

	def gson = new GsonBuilder()
		.registerTypeAdapter(BigDecimal, new BigDecimalTypeAdapter())
		.registerTypeAdapter(BigInteger, new BigIntegerTypeAdapter())
		.create()

	@Unroll
	def "测试 BigDecimalTypeAdapter 序列化: #input -> #expected"() {
		expect:
		gson.toJson(input, BigDecimal) == expected

		where:
		input                        || expected
		new BigDecimal("123.45")     || '"123.45"'
		new BigDecimal("0.00000001") || '"0.00000001"' // 确保不输出科学计数法 1E-8
		null                         || "null"
	}

	@Unroll
	def "测试 BigDecimalTypeAdapter 反序列化: #json -> #expected"() {
		expect:
		gson.fromJson(json, BigDecimal) == expected

		where:
		json        || expected
		"123.45"    || new BigDecimal("123.45") // NUMBER token
		'"123.45"'  || new BigDecimal("123.45") // STRING token
		'"invalid"' || null // 格式非法返回 null
		"null"      || null
	}

	@Unroll
	def "测试 BigIntegerTypeAdapter 序列化: #input -> #expected"() {
		expect:
		gson.toJson(input, BigInteger) == expected

		where:
		input                 || expected
		new BigInteger("123") || '"123"'
		null                  || "null"
	}

	@Unroll
	def "测试 BigIntegerTypeAdapter 反序列化: #json -> #expected"() {
		expect:
		gson.fromJson(json, BigInteger) == expected

		where:
		json        || expected
		"123"       || new BigInteger("123") // NUMBER token
		'"123"'     || new BigInteger("123") // STRING token
		'"invalid"' || null // 格式非法返回 null
		"null"      || null
	}
}
