package io.github.pangju666.commons.lang.utils

import io.github.pangju666.commons.lang.enums.RegExFlag
import spock.lang.Specification
import spock.lang.Unroll

import java.util.regex.Pattern

class RegExUtilsSpec extends Specification {
	@Unroll
	def "测试正则标志位计算 - 输入: #flags 预期值: #expected"() {
		expect:
		RegExUtils.computeFlags(flags as RegExFlag[]) == expected

		where:
		flags                                   || expected
		[]                                      || 0
		[RegExFlag.CASE_INSENSITIVE]            || Pattern.CASE_INSENSITIVE
		[RegExFlag.MULTILINE, RegExFlag.DOTALL] || computeFlag(Pattern.MULTILINE, Pattern.DOTALL)
	}

	@Unroll
	def "测试带标志位重新编译模式 - 原始模式: #originalFlags 新标志: #newFlags"() {
		given:
		def originalPattern = Pattern.compile("test", originalFlags)

		when:
		def newPattern = RegExUtils.compile(originalPattern, newFlags)

		then:
		newPattern.flags() == newFlags

		where:
		originalFlags            || newFlags
		Pattern.CASE_INSENSITIVE || Pattern.MULTILINE
		Pattern.DOTALL           || computeFlag(Pattern.CASE_INSENSITIVE, Pattern.UNICODE_CASE)
	}

	@Unroll
	def "测试正则编译起止匹配 - 输入: #regex 起止: #start/#end 预期模式: #expected"() {
		when:
		def pattern = RegExUtils.compile(regex, start, end)

		then:
		pattern.pattern() == expected

		where:
		regex    | start | end   | expected
		"a.b"    | true  | true  | "^a.b\$"
		"^a.b"   | true  | false | "^a.b"
		"a.b\$"  | false | true  | "a.b\$"
		"^a.b\$" | true  | true  | "^a.b\$"
		".*"     | true  | true  | "^.*\$"
	}

	@Unroll
	def "测试完全匹配验证 - 模式: #pattern 字符串: #input 预期: #expected"() {
		expect:
		RegExUtils.matches(pattern, input) == expected

		where:
		pattern  | input  | expected
		"^a.b\$" | "aab"  | true
		"^a.b\$" | "aabx" | false
		"\\d+"   | "123"  | true
		"\\d+"   | "12a3" | false
	}

	@Unroll
	def "测试查找所有匹配项 - 模式: #pattern 字符串: #input 预期: #expected"() {
		expect:
		RegExUtils.find(Pattern.compile(pattern), input) == expected

		where:
		pattern | input         | expected
		"\\d+"  | "a1b22c333"   | ["1", "22", "333"]
		"[A-Z]" | "Hello World" | ["H", "W"]
		"xxx"   | "abcdefg"     | []
	}

	@Unroll
	def "测试带标志位的正则编译 - 标志: #flags"() {
		when:
		def pattern = RegExUtils.compile("a.b", flags, true, true)

		then:
		(pattern.flags() & flags) == flags

		where:
		flags << [Pattern.CASE_INSENSITIVE, Pattern.MULTILINE, Pattern.DOTALL]
	}

	def "测试空输入处理"() {
		expect:
		RegExUtils.find("\\d+", "") == []
		RegExUtils.matches("^.*\$", "")
	}

	int computeFlag(int ... flags) {
		int result = flags[0]
		for (i in 1..<flags.length) {
			result |= flags[i]
		}
		return result
	}
}
