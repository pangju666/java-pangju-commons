package io.github.pangju666.commons.lang.utils

import io.github.pangju666.commons.lang.pool.Constants
import org.apache.commons.lang3.time.DateFormatUtils as ApacheDateFormatUtils
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant

class DateUtilsSpec extends Specification {
	@Unroll
	def "测试parseDate - 输入: #input 匹配模式: #pattern"() {
		when:
		def d = DateUtils.parseDate(input)

		then:
		d != null
		ApacheDateFormatUtils.format(d, pattern) == input

		where:
		input                 | pattern
		"2023-12-31"          | Constants.DATE_FORMAT
		"2023-12-31 23:59:12" | Constants.DATETIME_FORMAT
	}

	@Unroll
	def "测试parseDateOrDefault(预设模式) - 输入: #input"() {
		given:
		def date = DateUtils.parseDate(input)

		expect:
		(date == null) == isNull

		where:
		input              || isNull
		"2023-12-31"       || false
		"2023-12-31 23:59" || false
		""                 || true
		null               || true
	}

	@Unroll
	def "测试parseDateOrDefault(自定义模式) - 输入: #input, 模式: #pattern"() {
		given:
		def defaultDate = new Date(0)

		expect:
		def d = DateUtils.parseDateOrDefault(input, defaultDate, pattern)
		ApacheDateFormatUtils.format(d, pattern) == expected

		where:
		input        | pattern      || expected
		"01-02-2023" | "dd-MM-yyyy" || "01-02-2023"
		"2023/12/31" | "yyyy/MM/dd" || "2023/12/31"
	}

	def "测试nowDate 返回当前时间"() {
		expect:
		Math.abs(System.currentTimeMillis() - DateUtils.nowDate().time) < 2000
	}

	@Unroll
	def "测试toDate(Long) 与默认值 - 时间戳: #timestamp"() {
		given:
		def defDate = new Date(12345L)

		expect:
		DateUtils.toDate(timestamp) == expectedDirect
		DateUtils.toDate(timestamp, defDate) == expectedWithDef

		where:
		timestamp || expectedDirect || expectedWithDef
		0L        || new Date(0L)   || new Date(0L)
		null      || null           || new Date(12345L)
	}

	@Unroll
	def "测试toDate(Instant) 与默认值 - instant: #instant"() {
		given:
		def defDate = new Date(54321L)

		expect:
		DateUtils.toDate(instant) == expectedDirect
		DateUtils.toDate(instant, defDate) == expectedWithDef

		where:
		instant                  || expectedDirect || expectedWithDef
		Instant.ofEpochMilli(0L) || new Date(0L)   || new Date(0L)
		null                     || null           || new Date(54321L)
	}

	@Unroll
	def "测试getTime(Date) 与默认值 - date: #date"() {
		given:
		def defTime = 9999L

		expect:
		DateUtils.getTime(date) == expectedDirect
		DateUtils.getTime(date, defTime) == expectedWithDef

		where:
		date         || expectedDirect || expectedWithDef
		new Date(0L) || 0L             || 0L
		null         || null           || 9999L
	}

	@Unroll
	def "测试toInstant(Date) 与默认值 - date: #date"() {
		given:
		def defInstant = Instant.ofEpochMilli(321L)

		expect:
		DateUtils.toInstant(date) == expectedDirect
		DateUtils.toInstant(date, defInstant) == expectedWithDef

		where:
		date         || expectedDirect           || expectedWithDef
		new Date(0L) || Instant.ofEpochMilli(0L) || Instant.ofEpochMilli(0L)
		null         || null                     || Instant.ofEpochMilli(321L)
	}

	@Unroll
	def "测试betweenXxx - 毫秒差: #millis"() {
		given:
		def d1 = new Date(0L)
		def d2 = new Date(millis)

		expect:
		DateUtils.betweenMillis(d1, d2) == millis
		DateUtils.betweenSeconds(d1, d2) == Math.floorDiv(millis, 1000)
		DateUtils.betweenMinutes(d1, d2) == Math.floorDiv(millis, 1000 * 60)
		DateUtils.betweenHours(d1, d2) == Math.floorDiv(millis, 1000 * 60 * 60)
		DateUtils.betweenDays(d1, d2) == Math.floorDiv(millis, 1000 * 60 * 60 * 24)

		where:
		millis << [1000L, 60_000L, 3_600_000L, 86_400_000L]
	}

	@Unroll
	def "测试truncateBetweenXxx - 日期1: #d1Str 日期2: #d2Str"() {
		given:
		def d1 = DateUtils.parseDate(d1Str)
		def d2 = DateUtils.parseDate(d2Str)

		expect:
		DateUtils.truncateBetweenYears(d1, d2) == years
		DateUtils.truncateBetweenMonths(d1, d2) == months
		DateUtils.truncateBetweenDays(d1, d2) == days
		DateUtils.truncateBetweenHours(d1, d2) == hours
		DateUtils.truncateBetweenMinutes(d1, d2) == minutes
		DateUtils.truncateBetweenSeconds(d1, d2) == seconds

		where:
		d1Str              | d2Str              || years | months | days | hours | minutes | seconds
		"2020-12-31 23:59" | "2023-01-01 00:00" || 3     | 11     | 30   | 11    | 59      | 0
		"2023-01-01 00:00" | "2023-12-31 23:59" || 0     | 11     | 30   | 11    | 59      | 0
	}

	def "测试truncateBetween(通用) - minutes 字段"() {
		given:
		def c1 = Calendar.getInstance()
		def c2 = Calendar.getInstance()
		c1.set(Calendar.MINUTE, 10)
		c2.set(Calendar.MINUTE, 25)

		expect:
		DateUtils.truncateBetween(c1.time, c2.time, Calendar.MINUTE) == 15
	}

	def "测试calculateAge - 基于当前年份"() {
		given:
		def nowYear = Calendar.getInstance().get(Calendar.YEAR)
		def c = Calendar.getInstance()
		c.set(Calendar.YEAR, nowYear - 20)

		expect:
		DateUtils.calculateAge(c.time) == 20
	}
}
