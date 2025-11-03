package io.github.pangju666.commons.lang.utils

import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.Charset

class StringUtilsSpec extends Specification {
	@Unroll
	def "测试字符集转换(目标字符集) - 输入: '#input' 目标字符集: #targetCharset"() {
		setup:
		def result = StringUtils.convertCharset(input, targetCharset)

		expect:
		result == expected

		where:
		input         | targetCharset            | expected
		null          | Charset.forName("UTF-8") | null
		""            | Charset.forName("GBK")   | ""
		"Hello World" | Charset.forName("UTF-8") | "Hello World"  // 相同字符集
		"中文测试"    | Charset.forName("GBK")   | new String("中文测试".getBytes(), Charset.forName("GBK"))
	}

	@Unroll
	def "测试字符集转换(指定源字符集) - 输入: '#input' 源字符集: #srcCharset 目标字符集: #targetCharset"() {
		setup:
		def result = StringUtils.convertCharset(input, srcCharset, targetCharset)

		expect:
		result == expected

		where:
		input          | srcCharset               | targetCharset            | expected
		"测试文本"     | Charset.forName("UTF-8") | Charset.forName("GBK")   | new String("测试文本".getBytes(Charset.forName("UTF-8")), Charset.forName("GBK"))
		null           | Charset.forName("UTF-8") | Charset.forName("GBK")   | null
		""             | Charset.forName("GBK")   | Charset.forName("UTF-8") | ""
		"Same Charset" | Charset.forName("UTF-8") | Charset.forName("UTF-8") | "Same Charset"
	}

	@Unroll
	def "测试获取非空元素列表 - 输入集合: #input"() {
		expect:
		StringUtils.getNotBlankElements(input) == expected

		where:
		input                      | expected
		null                       | []
		[]                         | []
		["", "  ", null]           | []
		["a", "", "b", "  ", null] | ["a", "b"]
		["  test  ", "trim"]       | ["  test  ", "trim"]
	}

	@Unroll
	def "测试获取唯一非空元素列表 - 输入集合: #input"() {
		expect:
		StringUtils.getUniqueNotBlankElements(input) == expected

		where:
		input                      | expected
		null                       | []
		[]                         | []
		["a", "a", "", "A"]        | ["a", "A"]
		["test", " test ", "test"] | ["test", " test "]
		["", "  ", null, null]     | []
	}

	def "测试字符集转换异常场景"() {
		when:
		StringUtils.convertCharset("test", null)

		then:
		thrown(NullPointerException)

		when:
		StringUtils.convertCharset("test", Charset.defaultCharset(), null)

		then:
		thrown(NullPointerException)
	}
}
