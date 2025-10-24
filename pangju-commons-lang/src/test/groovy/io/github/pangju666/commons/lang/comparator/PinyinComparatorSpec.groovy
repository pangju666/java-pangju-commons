package io.github.pangju666.commons.lang.comparator


import io.github.pangju666.commons.lang.id.SnowflakeIdWorker
import io.github.pangju666.commons.lang.utils.DateUtils
import spock.lang.Specification

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
		// 根据机器ID(0-31)和数据中心ID(0-31)构建示例
		SnowflakeIdWorker idWorker1 = new SnowflakeIdWorker(0, 0);
		println idWorker1.nextId(); // 1981821050584825856


		// 根据机器ID(0-31)、数据中心ID(0-31)和初始时间戳构建示例
		SnowflakeIdWorker idWorker3 = new SnowflakeIdWorker(0, 0, DateUtils.addHours(new Date(), -1).getTime());
		println idWorker3.nextId(); // OHZZDEy2sqw75QlxWEiLL
	}


}
