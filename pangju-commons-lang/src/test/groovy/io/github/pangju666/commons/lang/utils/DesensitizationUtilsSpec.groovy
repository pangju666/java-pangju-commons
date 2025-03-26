package io.github.pangju666.commons.lang.utils

import spock.lang.Specification
import spock.lang.Unroll

class DesensitizationUtilsSpec extends Specification {
	// 通用方法测试
	@Unroll
	def "测试hideRound方法 - 输入：#input 保留前#pre后#suf位 => #expected"() {
		expect:
		DesensitizationUtils.hideRound(input, pre, suf) == expected

		where:
		input        | pre | suf | expected
		"12345"      | 3   | 3   | "12345"
		"1234567890" | 3   | 2   | "123*****90"
		"ABCDEF"     | 1   | 1   | "A****F"
		"短"         | 1   | 1   | "短"
	}

	// 身份证脱敏测试
	@Unroll
	def "测试hideIdCardNumber方法 - 输入：#input => #expected"() {
		expect:
		DesensitizationUtils.hideIdCardNumber(input) == expected

		where:
		input                | expected
		"110101199003077654" | "1****************4"
		"123"                | "1*3"
		null                 | null
	}

	// 手机号脱敏测试
	@Unroll
	def "测试hidePhoneNumber方法 - 输入：#input => #expected"() {
		expect:
		DesensitizationUtils.hidePhoneNumber(input) == expected

		where:
		input         | expected
		"13812345678" | "138******78"
		"1390000"     | "139**00"
		"invalid"     | "inv**id"
	}

	// 邮箱脱敏测试
	@Unroll
	def "测试hideEmail方法 - 输入：#input => #expected"() {
		expect:
		DesensitizationUtils.hideEmail(input) == expected

		where:
		input              | expected
		"test@example.com" | "tes*@example.com"
		"admin@domain.cn"  | "adm**@domain.cn"
		"invalid-email"    | "invalid-email"
	}

	// 地址脱敏测试
	@Unroll
	def "测试hideAddress方法 - 输入：#input => #expected"() {
		expect:
		DesensitizationUtils.hideAddress(input) == expected

		where:
		input                     | expected
		"北京市朝阳区建国路100号" | "北京市朝阳区*******"
		"上海市浦东新区"          | "上海市浦东新区"
		"广州天河区"              | "广州天河区"
	}

	// 中文姓名脱敏测试
	@Unroll
	def "测试hideChineseName方法 - 输入：#input => #expected"() {
		expect:
		DesensitizationUtils.hideChineseName(input) == expected

		where:
		input           | expected
		"张三"          | "*三"
		"欧阳明日"      | "**明日"
		"尼古拉斯·赵四" | "尼****赵四"
	}

	// 车牌号脱敏测试
	@Unroll
	def "测试hidePlateNumber方法 - 输入：#input => #expected"() {
		expect:
		DesensitizationUtils.hidePlateNumber(input) == expected

		where:
		input       | expected
		"京A12345"  | "京A**345"
		"沪AD12345" | "沪A***345"
		"粤Z00001"  | "粤Z**001"
	}

	// 密码脱敏测试
	@Unroll
	def "测试hidePassword方法 - 输入：#input => #expected"() {
		expect:
		DesensitizationUtils.hidePassword(input) == expected

		where:
		input    | expected
		"123456" | "******"
		null | null
		""       | ""
	}

	// 边界条件测试
	def "测试极端情况处理"() {
		expect:
		DesensitizationUtils.hideRound("", 1, 1) == ""
		DesensitizationUtils.hideLeft("TEST", 5) == "TEST"
		DesensitizationUtils.hideRight("TEST", -1) == "TEST"
	}

	// 社保卡脱敏测试
	@Unroll
	def "测试hideSocialSecurityCardNumber方法 - 输入：#input => #expected"() {
		expect:
		DesensitizationUtils.hideSocialSecurityCardNumber(input) == expected

		where:
		input       | expected
		"123456789" | "123***789"  // 9位长度
		"12345678"  | "123***78"    // 8位长度
		"12345"     | "12**5"     // 5位长度
	}

	// 车辆信息脱敏测试
	@Unroll
	def "测试hideVehicleFrameNumber方法 - 输入：#input => #expected"() {
		expect:
		DesensitizationUtils.hideVehicleFrameNumber(input) == expected

		where:
		input              | expected
		"ABCDEF1234567890" | "ABC**********890"  // 17位VIN
		"123"              | "123"  // 短于6位
	}

	// 性能测试
	def "测试长字符串处理性能"() {
		given:
		def longStr = "A" * 1000

		when:
		def result = DesensitizationUtils.hidePassword(longStr)

		then:
		result.length() == 1000
		result == "*" * 1000
	}

	// 特殊字符测试
	def "测试包含特殊字符的脱敏"() {
		expect:
		DesensitizationUtils.hideEmail("tést@例子.中国") == "tés*@例子.中国"
		DesensitizationUtils.hidePlateNumber("沪A·1234") == "沪A**234"
	}

	// 国际化测试
	def "测试多语言姓名脱敏"() {
		expect:
		DesensitizationUtils.hideChineseName("김첨지") == "*첨지"
		DesensitizationUtils.hideChineseName("佐藤·美和子") == "****和子"
	}
}
