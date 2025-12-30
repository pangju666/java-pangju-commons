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

package io.github.pangju666.commons.lang.comparator


import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("PinyinComparator 单元测试")
class PinyinComparatorSpec extends Specification {

	@Unroll
	def "测试 compare 方法: o1=#o1, o2=#o2 应返回 #result"() {
		given:
		def comparator = new PinyinComparator()

		expect:
		Integer.signum(comparator.compare(o1, o2)) == result

		where:
		o1     | o2     || result
		"a"    | "a"    || 0
		"a"    | "b"    || -1
		"b"    | "a"    || 1
		"你好" | "你好" || 0
		"你好" | "他好" || -1 // nihao vs tahao
		"他好" | "你好" || 1
		null   | null   || 0
		null   | "a"    || -1
		"a"    | null   || 1
		""     | ""     || 0
		""     | "a"    || -1
		"a"    | ""     || 1
		" "    | " "    || 0
		" "    | "  "   || -1  // 均为blank，使用长度比较
		" "    | "a"    || -1
		"a"    | " "    || 1
		null   | ""     || -1
		""     | " "    || -1
		" "    | "a"    || -1
		"张三" | "李四" || 1 // zhangsan vs lisi
		"阿"   | "波"   || -1 // a vs bo
	}

	def "测试 order(List) 方法"() {
		given:
		def list = ["李四", "张三", "王五", null, "", " ", "  "]

		when:
		PinyinComparator.order(list)

		then:
		// null -> "" -> " " -> "  " (稳定排序，原顺序保持) -> "李四"(lisi) -> "王五"(wangwu) -> "张三"(zhangsan)
		// lisi < wangwu < zhangsan ?
		// lisi vs wangwu: l < w.
		// wangwu vs zhangsan: w < z.
		list == [null, "", " ", "  ", "李四", "王五", "张三"]
	}

	def "测试 order(List) 方法 - 指定分隔符"() {
		given:
		def list = ["西安", "先"]
		// 西安 -> xi an
		// 先 -> xian
		// "xi an" < "xian" (space < 'a') ? No.
		// space (32) < 'a' (97).
		// So "xi an" < "xian".

		when:
		PinyinComparator.order(list, " ")

		then:
		list == ["西安", "先"]
	}

	def "测试 order(List) 方法 - 边界情况"() {
		when: "列表为 null"
		PinyinComparator.order((List) null)

		then: "不抛出异常"
		noExceptionThrown()

		when: "列表为空"
		def emptyList = []
		PinyinComparator.order(emptyList)

		then: "列表仍为空"
		emptyList.isEmpty()
	}

	def "测试 order(String[]) 方法"() {
		given:
		String[] array = ["李四", "张三", "王五", null, "", " ", "  "]

		when:
		PinyinComparator.order(array)

		then:
		Arrays.compare(array, [null, "", " ", "  ", "李四", "王五", "张三"] as String[]) == 0
	}

	def "测试 order(String[]) 方法 - 指定分隔符"() {
		given:
		String[] array = ["西安", "先"]

		when:
		PinyinComparator.order(array, "-")
		// "西安" -> "xi-an"
		// "先" -> "xian"
		// "xi-an" < "xian" ('-' < 'a')

		then:
		array == ["西安", "先"] as String[]
	}

	def "测试 order(String[]) 方法 - 边界情况"() {
		when: "数组为 null"
		PinyinComparator.order((String[]) null)

		then: "不抛出异常"
		noExceptionThrown()

		when: "数组为空"
		String[] emptyArray = new String[0]
		PinyinComparator.order(emptyArray)

		then: "数组仍为空"
		emptyArray.length == 0
	}
}
