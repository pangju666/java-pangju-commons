package io.github.pangju666.commons.lang.utils

import spock.lang.Specification
import spock.lang.Unroll

class TextFormatUtilsSpec extends Specification {
	@Unroll
	def "测试kebab-case转换 - 输入: '#input' 期望: '#expected'"() {
		expect:
		TextFormatUtils.formatAsKebabCase(input) == expected

		where:
		input             || expected
		null              || null
		""                || ""
		"hello_world"     || "hello-world"
		"HelloWorld"      || "hello-world"
		"XMLHttpRequest"  || "xmlhttp-request"
		"already-kebab"   || "already-kebab"
		"mix-ed_CaseText" || "mix-ed-case-text"
	}

	@Unroll
	def "测试snake_case转换 - 输入: '#input' 期望: '#expected'"() {
		expect:
		TextFormatUtils.formatAsSnakeCase(input) == expected

		where:
		input             || expected
		null              || null
		""                || ""
		"hello-world"     || "hello_world"
		"HelloWorld"      || "hello_world"
		"XMLHttpRequest"  || "xmlhttp_request"
		"already_snake"   || "already_snake"
		"mix-ed_CaseText" || "mix_ed_case_text"
	}

	@Unroll
	def "测试SCREAMING_SNAKE_CASE转换 - 输入: '#input' 期望: '#expected'"() {
		expect:
		TextFormatUtils.formatAsScreamingSnakeCase(input) == expected

		where:
		input            || expected
		null             || null
		""               || ""
		"hello-world"    || "HELLO_WORLD"
		"HelloWorld"     || "HELLO_WORLD"
		"XMLHttpRequest" || "XMLHTTP_REQUEST"
	}

	@Unroll
	def "测试SCREAMING-KEBAB-CASE转换 - 输入: '#input' 期望: '#expected'"() {
		expect:
		TextFormatUtils.formatAsScreamingKebabCase(input) == expected

		where:
		input            || expected
		null             || null
		""               || ""
		"hello_world"    || "HELLO-WORLD"
		"HelloWorld"     || "HELLO-WORLD"
		"XMLHttpRequest" || "XMLHTTP-REQUEST"
	}

	@Unroll
	def "测试camelCase转换(默认分隔符) - 输入: '#input' 期望: '#expected'"() {
		expect:
		TextFormatUtils.formatAsCamelCase(input) == expected

		where:
		input              || expected
		null               || null
		""                 || ""
		"hello-world-name" || "helloWorldName"
		"Hello-world"      || "helloWorld"
		"xml-http-request" || "xmlHttpRequest"
		"alreadyCamel"     || "alreadyCamel"
		"mix-ed-CaseText"  || "mixEdCaseText"
	}

	@Unroll
	def "测试camelCase转换(自定义分隔符) - 输入: '#input' 分隔符: '#delimiter' 期望: '#expected'"() {
		expect:
		TextFormatUtils.formatAsCamelCase(input, delimiter) == expected

		where:
		input               | delimiter || expected
		null                | "."       || null
		""                  | "."       || ""
		"user.name.id"      | "."       || "userNameId"
		"user_name_id_data" | "_"       || "userNameIdData"
		"Only-One"          | "-"       || "onlyOne"
	}

	@Unroll
	def "测试PascalCase转换(默认分隔符) - 输入: '#input' 期望: '#expected'"() {
		expect:
		TextFormatUtils.formatAsPascalCase(input) == expected

		where:
		input              || expected
		null               || null
		""                 || ""
		"hello-world-name" || "HelloWorldName"
		"xml-http-request" || "XmlHttpRequest"
		"already-Pascal"   || "AlreadyPascal"
	}

	@Unroll
	def "测试PascalCase转换(自定义分隔符) - 输入: '#input' 分隔符: '#delimiter' 期望: '#expected'"() {
		expect:
		TextFormatUtils.formatAsPascalCase(input, delimiter) == expected

		where:
		input               | delimiter || expected
		null                | "."       || null
		""                  | "."       || ""
		"user.name.id"      | "."       || "UserNameId"
		"user-name_id.data" | "-_."     || "UserNameIdData"
		"onlyone"           | "."       || "Onlyone"
	}

	@Unroll
	def "测试分隔符小写转换 - 输入: '#input' 分隔符: '#delimiter' 期望: '#expected'"() {
		expect:
		TextFormatUtils.delimiterLowerCase(input, delimiter as char) == expected

		where:
		input            | delimiter || expected
		"HelloWorld"     | "-"       || "hello-world"
		"XMLHttpRequest" | "-"       || "xmlhttp-request"
		"Already-Kebab"  | "-"       || "already-kebab"
		"snake_case"     | "-"       || "snake_case"
	}
}
