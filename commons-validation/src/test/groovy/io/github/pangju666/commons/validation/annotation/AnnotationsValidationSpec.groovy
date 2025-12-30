package io.github.pangju666.commons.validation.annotation

import io.github.pangju666.commons.lang.enums.RegExFlag
import jakarta.validation.Validation
import jakarta.validation.Validator
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("commons-validation 注解综合测试")
class AnnotationsValidationSpec extends Specification {
	static final Validator validator = Validation.buildDefaultValidatorFactory().validator

	private static boolean valid(Object bean) {
		validator.validate(bean).isEmpty()
	}

	@Unroll
	def "BASE64 校验: #v -> #expected"() {
		expect:
		valid(new Base64Bean(v)) == expected

		where:
		v                  || expected
		null               || true
		""                 || false
		" "                || false
		"SGVsbG8="         || true
		"SGVsbG8"          || true
		"QmFzZTY0LXRlc3Q=" || true
	}

	@Unroll
	def "Md5 校验: #v -> #expected"() {
		expect:
		valid(new Md5Bean(v)) == expected

		where:
		v                                  || expected
		null                               || true
		""                                 || false
		" "                                || false
		"d41d8cd98f00b204e9800998ecf8427e" || true
		"D41D8CD98F00B204E9800998ECF8427E" || true
		"not-md5"                          || false
		"123"                              || false
	}

	@Unroll
	def "HexColor 校验: #v -> #expected"() {
		expect:
		valid(new HexColorBean(v)) == expected

		where:
		v           || expected
		null        || true
		""          || false
		"#FFF"      || true
		"FFF"       || true
		"#FFFFFF"   || true
		"#FFFFFFFF" || true
		"#GGGGGG"   || false
		"#12345"    || false
	}

	@Unroll
	def "MimeType 校验: #v -> #expected"() {
		expect:
		valid(new MimeTypeBean(v)) == expected

		where:
		v                  || expected
		null               || true
		""                 || false
		"text/plain"       || true
		"image/png"        || true
		"application/json" || true
		"bad/type"         || true
	}

	@Unroll
	def "Identifier 校验: #v -> #expected"() {
		expect:
		valid(new IdentifierBean(v)) == expected

		where:
		v         || expected
		null      || true
		""        || false
		"abc"     || true
		"_abc123" || true
		"1abc"    || false
		"abc-123" || false
	}

	@Unroll
	def "HttpMethod 校验: #v -> #expected"() {
		expect:
		valid(new HttpMethodBean(v)) == expected

		where:
		v        || expected
		null     || true
		""       || false
		"GET"    || true
		"POST"   || true
		"DELETE" || true
		"TRACE"  || true
		"FOO"    || false
	}

	@Unroll
	def "ObjectId 校验: #v -> #expected"() {
		expect:
		valid(new ObjectIdBean(v)) == expected

		where:
		v                            || expected
		null                         || true
		""                           || false
		"507f1f77bcf86cd799439011"   || true
		"507f1f77bcf86cd79943901Z"   || false
		"507f1f77bcf86cd79943901122" || false
	}

	@Unroll
	def "ObjectIds 校验(allMatch=true): #v -> #expected"() {
		expect:
		valid(new ObjectIdsAllBean(v)) == expected

		where:
		v                                                        || expected
		null                                                     || true
		[]                                                       || true
		["507f1f77bcf86cd799439011"]                             || true
		["507f1f77bcf86cd799439011", "507f1f77bcf86cd799439012"] || true
		["507f1f77bcf86cd799439011", "bad"]                      || false
	}

	@Unroll
	def "ObjectIds 校验(allMatch=false): #v -> #expected"() {
		expect:
		valid(new ObjectIdsAnyBean(v)) == expected

		where:
		v                                   || expected
		null                                || true
		[]                                  || true
		["bad", "507f1f77bcf86cd799439011"] || true
		["bad1", "bad2"]                    || false
	}

	@Unroll
	def "NanoId 校验: #v -> #expected"() {
		expect:
		valid(new NanoIdBean(v)) == expected

		where:
		v                       || expected
		null                    || true
		""                      || false
		"abc123_-xyzABC123_-xy" || true
		"!"                     || false
	}

	@Unroll
	def "NanoIds 校验(allMatch=true): #v -> #expected"() {
		expect:
		valid(new NanoIdsAllBean(v)) == expected

		where:
		v                                      || expected
		null                                   || true
		[]                                     || true
		["abc123_-xyzABC123_-xy"]              || true
		["abc123_-xyzABC123_-xy", "not*valid"] || false
	}

	@Unroll
	def "NanoIds 校验(allMatch=false): #v -> #expected"() {
		expect:
		valid(new NanoIdsAnyBean(v)) == expected

		where:
		v                                      || expected
		null                                   || true
		[]                                     || true
		["not*valid", "abc123_-xyzABC123_-xy"] || true
		["!", "@"]                             || false
	}

	@Unroll
	def "UUIDS 校验(默认配置, allMatch=true): #v -> #expected"() {
		expect:
		valid(new UUIDSAllBean(v)) == expected

		where:
		v                                               || expected
		null                                            || true
		[]                                              || true
		["00000000-0000-0000-0000-000000000000"]        || true
		["123e4567-e89b-12d3-a456-426614174000"]        || true
		["123e4567-e89b-12d3-a456-426614174000", "bad"] || false
	}

	@Unroll
	def "UUIDS 校验(默认配置, allMatch=false): #v -> #expected"() {
		expect:
		valid(new UUIDSAnyBean(v)) == expected

		where:
		v                                               || expected
		null                                            || true
		[]                                              || true
		["bad", "123e4567-e89b-12d3-a456-426614174000"] || true
		["bad1", "bad2"]                                || false
	}

	@Unroll
	def "ChineseName 校验: #v -> #expected"() {
		expect:
		valid(new ChineseNameBean(v)) == expected

		where:
		v        || expected
		null     || true
		""       || false
		"张三"   || true
		"阿布都" || true
		"Zhang"  || false
		"张三1"  || false
	}

	@Unroll
	def "PhoneNumber 校验(MIX/strong=#strong): #v -> #expected"() {
		expect:
		valid(new PhoneMixBean(v, strong)) == expected

		where:
		v              | strong || expected
		null           | false  || true
		""             | false  || false
		"13800138000"  | false  || true
		"13800138000"  | true   || true
		"010-88888888" | false  || true
		"not phone"    | false  || false
	}

	@Unroll
	def "PhoneNumber 校验(MOBILE): #v -> #expected"() {
		expect:
		valid(new PhoneMobileBean(v)) == expected

		where:
		v              || expected
		null           || true
		""             || false
		"13800138000"  || true
		"010-88888888" || false
	}

	@Unroll
	def "PhoneNumber 校验(TEL): #v -> #expected"() {
		expect:
		valid(new PhoneTelBean(v)) == expected

		where:
		v              || expected
		null           || true
		""             || false
		"010-88888888" || true
		"13800138000"  || false
	}

	@Unroll
	def "Xss 校验: #v -> #expected"() {
		expect:
		valid(new XssBean(v)) == expected

		where:
		v                           || expected
		null                        || true
		""                          || false
		"<script>alert(1)</script>" || true
		"<b>text</b>"               || true
		"plain text"                || false
	}

	@Unroll
	def "RequestPath 校验: #v -> #expected"() {
		expect:
		valid(new RequestPathBean(v)) == expected

		where:
		v               || expected
		null            || true
		""              || false
		"/api/v1/users" || true
		"users/list"    || false
		"http://x"      || false
		"api//v1"       || false
	}

	@Unroll
	def "Filename 校验(extension=#ext): #v -> #expected"() {
		expect:
		valid(new FilenameBean(v, ext)) == expected

		where:
		v              | ext   || expected
		null           | true  || true
		""             | true  || false
		"readme.txt"   | true  || true
		"readme"       | true  || true
		"readme"       | false || true
		"bad:name.txt" | true  || false
	}

	@Unroll
	def "IP 校验(ipv6=#ipv6): #v -> #expected"() {
		expect:
		(ipv6 ? valid(new IPBeanV6(v)) : valid(new IPBeanV4(v))) == expected

		where:
		v                                         | ipv6  || expected
		null                                      | false || true
		""                                        | false || false
		"192.168.1.1"                             | false || true
		"256.256.256.256"                         | false || false
		"2001:0db8:85a3:0000:0000:8a2e:0370:7334" | true  || true
		"2001:db8::8a2e:370:7334"                 | true  || true
		"gggg:db8::8a2e:370:7334"                 | true  || false
	}

	@Unroll
	def "Number 校验(decimal=#decimal, positive=#positive): #v -> #expected"() {
		expect:
		valid(new NumberBean(v, decimal, positive)) == expected

		where:
		v         | decimal | positive || expected
		null      | false   | false    || true
		""        | false   | false    || false
		"123"     | false   | false    || true
		"-123"    | false   | false    || true
		"123.45"  | true    | false    || true
		"-123.45" | true    | false    || true
		"123"     | false   | true     || true
		"-123"    | false   | true     || false
		"123.45"  | true    | true     || true
		"-123.45" | true    | true     || false
		"abc"     | true    | false    || false
	}

	@Unroll
	def "NotBlankElements 校验(allMatch=#allMatch): #v -> #expected"() {
		expect:
		(allMatch ? valid(new NotBlankAllBean(v)) : valid(new NotBlankAnyBean(v))) == expected

		where:
		v          | allMatch || expected
		null       | true     || true
		[]         | true     || true
		["a", "b"] | true     || true
		["a", ""]  | true     || false
		["", "a"]  | false    || true
		["", ""]   | false    || false
	}

	@Unroll
	def "PatternElements 校验(allMatch=#allMatch, flags=#flags): #v -> #expected"() {
		expect:
		(allMatch ? valid(new PatternAllBean(v, flags)) : valid(new PatternAnyBean(v, flags))) == expected

		where:
		v              | allMatch | flags                        || expected
		null           | true     | []                           || true
		[]             | true     | []                           || true
		["123", "456"] | true     | []                           || true
		["abc", "123"] | true     | []                           || false
		["abc", "DEF"] | true     | [RegExFlag.CASE_INSENSITIVE] || false
		["abc", "123"] | false    | []                           || true
	}

	@Unroll
	def "EnumName 校验(ignoreCase=#ignoreCase): #v -> #expected"() {
		expect:
		(ignoreCase ? valid(new EnumNameCaseInsensitiveBean(v)) : valid(new EnumNameCaseSensitiveBean(v))) == expected

		where:
		v      | ignoreCase || expected
		null   | true       || true
		""     | true       || false
		"RED"  | true       || true
		"red"  | true       || true
		"red"  | false      || false
		"BLUE" | false      || true
	}

	static class Base64Bean {
		@BASE64
		String v

		Base64Bean(String v) { this.v = v }
	}

	static class Md5Bean {
		@Md5
		String v

		Md5Bean(String v) { this.v = v }
	}

	static class HexColorBean {
		@HexColor
		String v

		HexColorBean(String v) { this.v = v }
	}

	static class MimeTypeBean {
		@MimeType
		String v

		MimeTypeBean(String v) { this.v = v }
	}

	static class IdentifierBean {
		@Identifier
		String v

		IdentifierBean(String v) { this.v = v }
	}

	static class HttpMethodBean {
		@HttpMethod
		String v

		HttpMethodBean(String v) { this.v = v }
	}

	static class ObjectIdBean {
		@ObjectId
		String v

		ObjectIdBean(String v) { this.v = v }
	}

	static class ObjectIdsAllBean {
		@ObjectIds(allMatch = true)
		List<String> v

		ObjectIdsAllBean(List<String> v) { this.v = v }
	}

	static class ObjectIdsAnyBean {
		@ObjectIds(allMatch = false)
		List<String> v

		ObjectIdsAnyBean(List<String> v) { this.v = v }
	}

	static class NanoIdBean {
		@NanoId
		String v

		NanoIdBean(String v) { this.v = v }
	}

	static class NanoIdsAllBean {
		@NanoIds(allMatch = true)
		List<String> v

		NanoIdsAllBean(List<String> v) { this.v = v }
	}

	static class NanoIdsAnyBean {
		@NanoIds(allMatch = false)
		List<String> v

		NanoIdsAnyBean(List<String> v) { this.v = v }
	}

	static class UUIDSAllBean {
		@UUIDS(allMatch = true)
		List<String> v

		UUIDSAllBean(List<String> v) { this.v = v }
	}

	static class UUIDSAnyBean {
		@UUIDS(allMatch = false)
		List<String> v

		UUIDSAnyBean(List<String> v) { this.v = v }
	}

	static class ChineseNameBean {
		@ChineseName
		String v

		ChineseNameBean(String v) { this.v = v }
	}

	static class PhoneMixBean {
		@PhoneNumber(type = PhoneNumber.Type.MIX, strong = true)
		String vStrong
		@PhoneNumber(type = PhoneNumber.Type.MIX, strong = false)
		String vWeak

		PhoneMixBean(String v, boolean strong) { if (strong) vStrong = v else vWeak = v }
	}

	static class PhoneMobileBean {
		@PhoneNumber(type = PhoneNumber.Type.MOBILE)
		String v

		PhoneMobileBean(String v) { this.v = v }
	}

	static class PhoneTelBean {
		@PhoneNumber(type = PhoneNumber.Type.TEL)
		String v

		PhoneTelBean(String v) { this.v = v }
	}

	static class XssBean {
		@Xss
		String v

		XssBean(String v) { this.v = v }
	}

	static class RequestPathBean {
		@RequestPath
		String v

		RequestPathBean(String v) { this.v = v }
	}

	static class FilenameBean {
		@Filename(extension = true)
		String vExt
		@Filename(extension = false)
		String vNoExt

		FilenameBean(String v, boolean ext) { if (ext) vExt = v else vNoExt = v }
	}

	static class IPBeanV4 {
		@IP(ipv6 = false)
		String v

		IPBeanV4(String v) { this.v = v }
	}

	static class IPBeanV6 {
		@IP(ipv6 = true)
		String v

		IPBeanV6(String v) { this.v = v }
	}

	static class NumberBean {
		@Number(decimal = true, positive = true)
		String vPosDec
		@Number(decimal = false, positive = true)
		String vPosInt
		@Number(decimal = true, positive = false)
		String vDec
		@Number(decimal = false, positive = false)
		String vInt

		NumberBean(String v, boolean decimal, boolean positive) {
			if (decimal && positive) vPosDec = v
			else if (!decimal && positive) vPosInt = v
			else if (decimal && !positive) vDec = v
			else vInt = v
		}
	}

	static class NotBlankAllBean {
		@NotBlankElements(allMatch = true)
		List<String> v

		NotBlankAllBean(List<String> v) { this.v = v }
	}

	static class NotBlankAnyBean {
		@NotBlankElements(allMatch = false)
		List<String> v

		NotBlankAnyBean(List<String> v) { this.v = v }
	}

	static class PatternAllBean {
		@PatternElements(regexp = "\\d+", allMatch = true)
		List<String> v

		PatternAllBean(List<String> v, List flags) { this.v = v }
	}

	static class PatternAnyBean {
		@PatternElements(regexp = "\\d+", allMatch = false)
		List<String> v

		PatternAnyBean(List<String> v, List flags) { this.v = v }
	}

	enum Color {
		RED, GREEN, BLUE
	}

	static class EnumNameCaseInsensitiveBean {
		@EnumName(enumClass = Color, ignoreCase = true)
		String v

		EnumNameCaseInsensitiveBean(String v) { this.v = v }
	}

	static class EnumNameCaseSensitiveBean {
		@EnumName(enumClass = Color, ignoreCase = false)
		String v

		EnumNameCaseSensitiveBean(String v) { this.v = v }
	}
}
