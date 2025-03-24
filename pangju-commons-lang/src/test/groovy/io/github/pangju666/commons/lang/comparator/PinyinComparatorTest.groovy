package io.github.pangju666.commons.lang.comparator


import spock.lang.Specification

class PinyinComparatorTest extends Specification {
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
		["天气如何???", "!!!!!测试字符串", "Best Java..."] || ["!!!!!测试字符串", "Best Java...", "天气如何???"]
	}
}
