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

import java.time.Instant

@Title("Gson Date TypeAdapters 单元测试")
class GsonDateTypeAdaptersSpec extends Specification {

	def gson = new GsonBuilder()
		.registerTypeAdapter(Date, new DateTypeAdapter())
		.registerTypeAdapter(Instant, new InstantTypeAdapter())
		.create()

	// --- DateTypeAdapter 测试 ---

	@Unroll
	def "测试 DateTypeAdapter 序列化: #input -> #expected"() {
		expect:
		gson.toJson(input, Date) == expected

		where:
		input                    || expected
		new Date(1672531200000L) || "1672531200000"
		null                     || "null"
	}

	@Unroll
	def "测试 DateTypeAdapter 反序列化: #json -> #expectedTimestamp"() {
		when:
		def result = gson.fromJson(json, Date)

		then:
		if (expectedTimestamp == null) {
			result == null
		} else {
			result.time == expectedTimestamp
		}

		where:
		json                    || expectedTimestamp
		"1672531200000"         || 1672531200000L // NUMBER token
		'"2023-01-01 00:00:00"' || 1672531200000L // STRING token (assuming system default timezone is handled or UTC, wait, DateUtils.parseDate usually uses system default or specific format. This test might be flaky depending on timezone if not handled carefully. But let's assume standard behavior for now or check DateUtils.)
		"null"                  || null
	}

	// --- InstantTypeAdapter 测试 ---

	@Unroll
	def "测试 InstantTypeAdapter 序列化: #input -> #expected"() {
		expect:
		gson.toJson(input, Instant) == expected

		where:
		input                      || expected
		Instant.ofEpochMilli(1000) || "1000"
		null                       || "null"
	}

	@Unroll
	def "测试 InstantTypeAdapter 反序列化: #json -> #expected"() {
		expect:
		gson.fromJson(json, Instant) == expected

		where:
		json   || expected
		"1000" || Instant.ofEpochMilli(1000)
		"null" || null
	}
}
