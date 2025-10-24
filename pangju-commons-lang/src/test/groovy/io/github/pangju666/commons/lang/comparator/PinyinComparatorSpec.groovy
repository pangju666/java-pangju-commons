package io.github.pangju666.commons.lang.comparator


import io.github.pangju666.commons.lang.utils.IdUtils
import spock.lang.Specification

import java.security.SecureRandom
import java.util.concurrent.ThreadLocalRandom

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
		// 使用自定义随机数生成器生成高性能标准格式UUID
		println IdUtils.simpleFastUUID(new SecureRandom()); // dd2796ab-6bf6-4ebf-a3bf-06cb947d9096

// 使用自定义随机数生成器生成高性能标准格式UUID
		println IdUtils.simpleFastUUID(ThreadLocalRandom.current()); // dd2796ab-6bf6-4ebf-a3bf-06cb947d9096
	}


}
