package io.github.pangju666.commons.lang.comparator


import io.github.pangju666.commons.lang.utils.RegExUtils
import spock.lang.Specification

import java.util.regex.Pattern

class PinyinComparatorSpec extends Specification {
	def "Compare"() {
		given:
		def comparator = new PinyinComparator()

		expect:
		list.toSorted(comparator) == result

		where:
		list                                               || result
		[null, "", "天气如何"]                             || [null, "", "天气如何"]
		["天气如何", null, ""]                             || [null, "", "天气如何"]
		["天气如何", "测试字符串", "Best Java"]            || ["Best Java", "测试字符串", "天气如何"]
		["天气如何", "12312313", "Best Java"] || ["12312313", "Best Java", "天气如何"]
		["天气如何???", "!!!!!测试字符串", "Best Java..."] || ["!!!!!测试字符串", "Best Java...", "天气如何???"]
	}

	def "test"() {
		setup:
		println RegExUtils.find(Pattern.compile("1\\d{10}"), "12345678911"); // true
		println RegExUtils.find(Pattern.compile("1\\d{10}"), "22345678911"); // false

		println RegExUtils.find("1\\d{10}", "12345678911"); // true
		println RegExUtils.find("1\\d{10}", "22345678911"); // false

		println RegExUtils.matches(Pattern.compile("^1\\d{10}\$"), "12345678911"); // true
		println RegExUtils.matches(Pattern.compile("^1\\d{10}\$"), "22345678911"); // false

		println RegExUtils.matches("^1\\d{10}\$", "12345678911"); // true
		println RegExUtils.matches("^1\\d{10}\$", "22345678911"); // false
	}


}
