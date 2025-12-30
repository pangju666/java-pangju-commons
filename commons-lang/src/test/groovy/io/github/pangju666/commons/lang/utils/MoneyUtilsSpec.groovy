package io.github.pangju666.commons.lang.utils

import spock.lang.Specification
import spock.lang.Unroll

class MoneyUtilsSpec extends Specification {

	@Unroll
	def "测试format(Double) - 输入: #input 期望: '#expected'"() {
		expect:
		MoneyUtils.format(input as Double) == expected

		where:
		input        || expected
		0d           || "0.00"
		1234567.891d || "1,234,567.89"
		-1234.5d     || "-1,234.50"
	}

	def "测试format(Double) - null输入返回空字符串"() {
		expect:
		MoneyUtils.format(null as Double) == ""
	}

	@Unroll
	def "测试format(BigDecimal) - 输入: #input 期望: '#expected'"() {
		expect:
		MoneyUtils.format(input as BigDecimal) == expected

		where:
		input                         || expected
		new BigDecimal("0")           || "0.00"
		new BigDecimal("1234567.891") || "1,234,567.89"
		new BigDecimal("-1234.5")     || "-1,234.50"
	}

	def "测试format(BigDecimal) - null输入返回空字符串"() {
		expect:
		MoneyUtils.format(null as BigDecimal) == ""
	}

	@Unroll
	def "测试convertToChinese(Double) - 输入: #input 期望: '#expected'"() {
		expect:
		MoneyUtils.convertToChinese(input as Double) == expected

		where:
		input          || expected
		null           || ""
		0d             || "零元整"
		10d            || "壹拾元整"
		1234567.89d    || "壹佰贰拾叁万肆仟伍佰陆拾柒元捌角玖分"
		-100200300.05d || "负壹亿贰拾万叁佰元零伍分"
	}

	@Unroll
	def "测试convertToChinese(BigDecimal) - 输入: #input 期望: '#expected'"() {
		expect:
		MoneyUtils.convertToChinese(input as BigDecimal) == expected

		where:
		input                           || expected
		null                            || ""
		new BigDecimal("0")             || "零元整"
		new BigDecimal("10")            || "壹拾元整"
		new BigDecimal("0.05")          || "零元零伍分"
		new BigDecimal("1.50")          || "壹元伍角"
		new BigDecimal("1234567.89")    || "壹佰贰拾叁万肆仟伍佰陆拾柒元捌角玖分"
		new BigDecimal("-100200300.05") || "负壹亿贰拾万叁佰元零伍分"
		new BigDecimal("100000001")     || "壹亿零壹元整"
	}

	@Unroll
	def "测试convertIntegerPartToChinese - 输入: #integerPart 期望: '#expected'"() {
		expect:
		MoneyUtils.convertIntegerPartToChinese(integerPart) == expected

		where:
		integerPart || expected
		0L          || "零"
		1L          || "壹"
		10L         || "壹拾"
		123L        || "壹佰贰拾叁"
		4567L       || "肆仟伍佰陆拾柒"
		1234567L    || "壹佰贰拾叁万肆仟伍佰陆拾柒"
		100000001L  || "壹亿零壹"
	}

	@Unroll
	def "测试convertSectionToChinese - 输入: #section 期望: '#expected'"() {
		expect:
		MoneyUtils.convertSectionToChinese(section) == expected

		where:
		section || expected
		5       || "伍"
		10      || "壹拾"
		50      || "伍拾"
		3050    || "叁仟零伍拾"
		1010    || "壹仟零壹拾"
		1005    || "壹仟零伍"
		1000    || "壹仟"
	}

	@Unroll
	def "测试convertDecimalPartToChinese - 输入: #decimalPart 期望: '#expected'"() {
		expect:
		MoneyUtils.convertDecimalPartToChinese(decimalPart) == expected

		where:
		decimalPart || expected
		0           || ""
		10          || "壹角"
		5           || "零伍分"
		50          || "伍角"
		15          || "壹角伍分"
	}
}

