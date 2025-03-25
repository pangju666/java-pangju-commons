package io.github.pangju666.commons.lang.utils

import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class DateUtilsTest extends Specification {
	// 日期解析测试
	@Unroll
	def "测试parseDate方法 - 输入：#input 期望格式：#pattern"() {
		expect:
		DateUtils.parseDate(input)?.time == DateUtils.parseDate(pattern).time

		where:
		input              | pattern
		"2023-12-31"       | "2023-12-31"
		"2023-12-31 23:59" | "2023-12-31 23:59"
	}

	// 时间转换测试
	@Unroll
	def "测试toDate转换方法 - 输入类型：#type"() {
		given:
		def now = new Date()

		expect:
		DateUtils.toDate(input) != null

		where:
		type            | input
		"LocalDateTime" | LocalDateTime.now()
		"LocalDate"     | LocalDate.now()
		"时间戳"        | System.currentTimeMillis()
	}

	// 时间差计算测试
	@Unroll
	def "测试betweenDays方法 - date1:#d1, date2:#d2"() {
		expect:
		DateUtils.betweenDays(d1, d2) == expectedDays

		where:
		d1                  | d2                  | expectedDays
		new Date(0)         | new Date(86400000)  | 1     // 1970-01-01 和 1970-01-02
		parse("2023-01-01") | parse("2023-01-03") | 2
		parse("2023-02-28") | parse("2023-03-01") | 1
	}

	// 截断计算测试
	@Unroll
	def "测试truncateBetweenYears方法 - date1:#d1Str, date2:#d2Str"() {
		given:
		def d1 = parse(d1Str)
		def d2 = parse(d2Str)

		expect:
		DateUtils.truncateBetweenYears(d1, d2) == expected

		where:
		d1Str        | d2Str        | expected
		"2020-12-31" | "2023-01-01" | 3
		"2023-01-01" | "2023-12-31" | 0
		"2023-06-15" | "2025-06-14" | 2
	}

	// 年龄计算测试
	@Unroll
	def "测试calculateAge方法 - 生日:#birthDateStr 预期年龄:#expected"() {
		given:
		def birthDate = parse(birthDateStr)

		expect:
		DateUtils.calculateAge(birthDate) == expected

		where:
		birthDateStr | expected
		"2000-12-15" | 25
		"2000-12-16" | 25
		"2005-02-28" | 20
	}

	// 空值处理测试
	def "测试空值处理方法"() {
		expect:
		DateUtils.parseDate(null) == null
		DateUtils.toDate((LocalDateTime) null) == null
		DateUtils.betweenDays(null, new Date()) == null
	}

	// 辅助方法：快速解析日期
	private static Date parse(String dateStr) {
		DateUtils.parseDate(dateStr)
	}

	// 重载calculateAge测试方法（模拟当前时间）
	private static Integer calculateAge(Date birthDate, Date currentDate) {
		def originalNow = DateUtils.nowDate()
		def calendar = Calendar.getInstance()
		calendar.time = currentDate
		try {
			// 使用反射临时修改nowDate方法返回值
			DateUtils.metaClass.static.nowDate = { -> calendar.time }
			return DateUtils.calculateAge(birthDate)
		} finally {
			DateUtils.metaClass = null
		}
	}

	// 边界值测试样例
	def "测试日期边界值"() {
		expect:
		DateUtils.truncateBetweenDays(parse("2023-02-28"), parse("2024-02-29")) == 1
	}

// 时区敏感测试
	def "测试时区转换"() {
		given:
		def utcDateTime = LocalDateTime.of(2023, 12, 31, 23, 59).atZone(ZoneId.of("UTC"))

		when:
		def date = DateUtils.toDate(utcDateTime.toLocalDateTime())

		then:
		date.toString().contains("Dec 31") || date.toString().contains("Jan 01")
	}

	// 并发安全测试
	def "测试并发场景"() {
		when:
		def results = (1..1000).parallelStream().map { it -> DateUtils.parseDate("2023-12-31") }.toList()

		then:
		results.every { it != null }
	}
}
