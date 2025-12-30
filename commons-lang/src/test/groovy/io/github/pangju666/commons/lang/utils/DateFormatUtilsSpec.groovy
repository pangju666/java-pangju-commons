package io.github.pangju666.commons.lang.utils

import io.github.pangju666.commons.lang.pool.Constants
import org.apache.commons.lang3.time.DateFormatUtils as ApacheDateFormatUtils
import spock.lang.Specification
import spock.lang.Unroll

class DateFormatUtilsSpec extends Specification {
	@Unroll
	def "测试formatDatetime(Date) 与 formatDatetime(Long) - 时间戳: #timestamp"() {
		given:
		def date = new Date(timestamp)

		expect:
		DateFormatUtils.formatDatetime(date) == ApacheDateFormatUtils.format(date, Constants.DATETIME_FORMAT)
		DateFormatUtils.formatDatetime(timestamp) == ApacheDateFormatUtils.format(timestamp, Constants.DATETIME_FORMAT)

		where:
		timestamp << [0L, 1735560000000L /* 2024-12-30T00:00:00+08:00 approx */, System.currentTimeMillis()]
	}

	def "测试formatDatetime() 当前时间格式匹配"() {
		when:
		def result = DateFormatUtils.formatDatetime()

		then:
		result ==~ /\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}/
	}

	@Unroll
	def "测试formatDate(Date) 与 formatDate(Long) - 时间戳: #timestamp"() {
		given:
		def date = new Date(timestamp)

		expect:
		DateFormatUtils.formatDate(date) == ApacheDateFormatUtils.format(date, Constants.DATE_FORMAT)
		DateFormatUtils.formatDate(timestamp) == ApacheDateFormatUtils.format(timestamp, Constants.DATE_FORMAT)

		where:
		timestamp << [0L, 1735560000000L, System.currentTimeMillis()]
	}

	def "测试formatDate() 当前日期格式匹配"() {
		when:
		def result = DateFormatUtils.formatDate()

		then:
		result ==~ /\d{4}-\d{2}-\d{2}/
	}

	@Unroll
	def "测试formatTime(Date) 与 formatTime(Long) - 时间戳: #timestamp"() {
		given:
		def date = new Date(timestamp)

		expect:
		DateFormatUtils.formatTime(date) == ApacheDateFormatUtils.format(date, Constants.TIME_FORMAT)
		DateFormatUtils.formatTime(timestamp) == ApacheDateFormatUtils.format(timestamp, Constants.TIME_FORMAT)

		where:
		timestamp << [0L, 1735560000000L, System.currentTimeMillis()]
	}

	@Unroll
	def "测试空值返回空字符串 - 方法: #method"() {
		expect:
		call() == ""

		where:
		method                 | call
		"formatDatetime(Date)" | { -> DateFormatUtils.formatDatetime((Date) null) }
		"formatDatetime(Long)" | { -> DateFormatUtils.formatDatetime((Long) null) }
		"formatDate(Date)"     | { -> DateFormatUtils.formatDate((Date) null) }
		"formatDate(Long)"     | { -> DateFormatUtils.formatDate((Long) null) }
		"formatTime(Date)"     | { -> DateFormatUtils.formatTime((Date) null) }
		"formatTime(Long)"     | { -> DateFormatUtils.formatTime((Long) null) }
	}
}
