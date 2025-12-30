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
		idCard                                                        | expected
		buildId18("110105", "19900307", "233")                        | true
		buildId18("440308", "20000229", "032")                        | true
		buildId18("110105", "19191231", "233")                        | false
		"11010519901307233X"                                          | false
		"11010519900230233X"                                          | false
		buildId18("110105", "19900307", "233").substring(0, 17) + "0" | false
		"12345678901234567"                                           | false
	}

	@Unroll
	def "测试性别解析功能 - 身份证号: #idCard 预期性别: #expected"() {
		expect:
		IdCardUtils.parseSex(idCard) == expected

		where:
		idCard                                 | expected
		buildId18("110105", "19900307", "233") | "男"
		buildId18("110105", "19900307", "232") | "女"
		"110105900307123"                      | "男"
		"110105900307124"                      | "女"
		"123"                                  | null
		null                                   | null
	}

	@Unroll
	def "测试出生日期解析功能 - 身份证号: #idCard 预期日期: #expected"() {
		expect:
		def actual = IdCardUtils.parseBirthDate(idCard)
		if (expected == null) {
			assert actual == null
		} else {
			assert actual == expected
		}

		where:
		idCard                                 | expected
		buildId18("110105", "19900307", "233") | LocalDate.of(1990, 3, 7)
		buildId18("440308", "20000229", "032") | LocalDate.of(2000, 2, 29)
		"110105900307123"                      | LocalDate.of(1990, 3, 7)
		"invalid"                              | null
	}

	private static String buildId18(String area, String birthYYYYMMDD, String seq3) {
		def base = area + birthYYYYMMDD + seq3
		int[] w = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
		char[] vc = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']
		int sum = 0
		for (int i = 0; i < 17; i++) {
			sum += (((int) base.charAt(i)) - ((int) '0')) * w[i]
		}
		def code = vc[sum % 11]
		return base + code
	}
}
