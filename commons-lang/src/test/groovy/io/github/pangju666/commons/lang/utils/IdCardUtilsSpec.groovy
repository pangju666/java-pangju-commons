package io.github.pangju666.commons.lang.utils

import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

class IdCardUtilsSpec extends Specification {
	@Unroll
	def "测试身份证验证功能 - 身份证号: #idCard 预期结果: #expected"() {
		expect:
		IdCardUtils.validate(idCard) == expected

		where:
		idCard               | expected
		// 合法18位身份证
		"11010519900307233X" | false   // 正确校验码
		"440308199901014032" | false   // 常规有效号码
		// 非法18位
		"11010519901307233X" | false  // 错误月份(13月)
		"11010519900230233X" | false  // 错误日期(2月30日)
		"110105199003072331" | false  // 校验码不匹配
		"12345678901234567"  | false  // 长度不足
	}

	@Unroll
	def "测试性别解析功能 - 身份证号: #idCard 预期性别: #expected"() {
		expect:
		IdCardUtils.parseSex(idCard) == expected

		where:
		idCard               | expected
		// 18位测试
		"11010519900307233X" | "男"  // 第17位奇数
		"440308199901014032" | "男"  // 第17位奇数
		"110105199003072342" | "女"  // 第17位偶数
		// 无效情况
		"123"                | null  // 无效长度
		null                 | null  // 空输入
	}

	@Unroll
	def "测试出生日期解析功能 - 身份证号: #idCard 预期日期: #expected"() {
		expect:
		def actual = IdCardUtils.parseBirthDate(idCard)
		if (expected == null) {
			assert actual == null
		} else {
			assert DateUtils.toLocalDate(actual) == expected
		}

		where:
		idCard               | expected
		// 18位日期解析
		"11010519900307233X" | LocalDate.of(1990, 3, 7)
		"44030820000229032X" | LocalDate.of(2000, 2, 29) // 闰年测试
		// 无效情况
		"invalid"            | null  // 无效格式
	}
}