/*
 *   Copyright 2025 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.pangju666.commons.lang.pool

import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("RegExPool 常用正则测试")
class RegExPoolSpec extends Specification {

	@Unroll
	def "HEX_COLOR 匹配: #input -> #result"() {
		expect:
		input.matches(RegExPool.HEX_COLOR) == result

		where:
		input       || result
		"#FFFFFF"   || true
		"FFF"       || true
		"#FFFFFFFF" || true
		"FFFF"      || true
		"#GGGGGG"   || false
		"#12345"    || false
	}

	@Unroll
	def "ENGLISH_CHARACTER(S) 匹配: #input -> #single/#multi"() {
		expect:
		input.matches(RegExPool.ENGLISH_CHARACTER) == single
		input.matches(RegExPool.ENGLISH_CHARACTERS) == multi

		where:
		input || single | multi
		"A"   || true   | true
		"ab"  || false  | true
		"9"   || false  | false
		"_"   || false  | false
	}

	@Unroll
	def "CHINESE_CHARACTER(S) 匹配: #input -> #single/#multi"() {
		expect:
		input.matches(RegExPool.CHINESE_CHARACTER) == single
		input.matches(RegExPool.CHINESE_CHARACTERS) == multi

		where:
		input  || single | multi
		"中"   || true   | true
		"中文" || false  | true
		"A"    || false  | false
	}

	@Unroll
	def "SYMBOLS_CHARACTER(S) 匹配: #input -> #single/#multi"() {
		expect:
		input.matches(RegExPool.SYMBOLS_CHARACTER) == single
		input.matches(RegExPool.SYMBOLS_CHARACTERS) == multi

		where:
		input || single | multi
		"!"   || true   | true
		"《》"  || false  | true
		"A"   || false  | false
	}

	@Unroll
	def "IPV4 严格匹配: #input -> #result"() {
		expect:
		input.matches(RegExPool.IPV4) == result

		where:
		input             || result
		"127.0.0.1"       || true
		"255.255.255.255" || true
		"999.0.0.1"       || false
		"127.0.0"         || false
	}

	def "IPV6 简单匹配"() {
		expect:
		"2001:0db8:85a3:0000:0000:8a2e:0370:7334".matches(RegExPool.IPV6)
	}

	@Unroll
	def "MONEY 金额匹配: #input -> #result"() {
		expect:
		input.matches(RegExPool.MONEY) == result

		where:
		input       || result
		"123"       || true
		"1,000"     || true
		"1,234.56"  || true
		"-0.9"      || true
		"12,34.56"  || false
		"1,234.567" || false
	}

	@Unroll
	def "EMAIL 匹配: #input -> #result"() {
		expect:
		input.matches(RegExPool.EMAIL) == result

		where:
		input                         || result
		"user@example.com"            || true
		"user.name+tag@example.co.uk" || true
		"user@"                       || false
		"@example.com"                || false
	}

	@Unroll
	def "MOBILE_PHONE_STRONG/WEAK 匹配: #input -> #strong/#weak"() {
		expect:
		input.matches(RegExPool.MOBILE_PHONE_STRONG) == strong
		input.matches(RegExPool.MOBILE_PHONE_WEAK) == weak

		where:
		input         || strong | weak
		"13812345678" || true   | true
		"19912345678" || true   | true
		"11123456789" || false  | true
		"1234567890"  || false  | false
	}

	@Unroll
	def "TEL_PHONE 座机匹配: #input -> #result"() {
		expect:
		input.matches(RegExPool.TEL_PHONE) == result

		where:
		input               || result
		"010-88888888"      || true
		"0571-1234567"      || true
		"0512-12345678-123" || true
		"0512-123456"       || false
	}

	@Unroll
	def "ID_CARD 身份证匹配: #input -> #result"() {
		expect:
		input.matches(RegExPool.ID_CARD) == result

		where:
		input                || result
		"11010519491231002X" || true   // 18位示例
		"130503670401001"    || true   // 15位示例
		"000000000000000"    || false
	}

	@Unroll
	def "ZIP_CODE 邮编匹配: #input -> #result"() {
		expect:
		input.matches(RegExPool.ZIP_CODE) == result

		where:
		input    || result
		"100000" || true
		"000000" || false
	}

	@Unroll
	def "URL/HTTP_URL/FTP_URL/FILE_URL 匹配: #input -> #url/#http/#ftp/#file"() {
		expect:
		input.matches(RegExPool.URL) == url
		input.matches(RegExPool.HTTP_URL) == http
		input.matches(RegExPool.FTP_URL) == ftp
		input.matches(RegExPool.FILE_URL) == file

		where:
		input                               || url  | http  | ftp   | file
		"https://example.com/path?q=1#frag" || true | true  | false | false
		"ftp://example.com/resource"        || true | false | true  | false
		"file:///C:/path/to/file.txt"       || true | false | false | true
		"mailto:user@example.com"           || true | false | false | false
	}

	@Unroll
	def "URI 通用匹配: #input -> #result"() {
		expect:
		input.matches(RegExPool.URI) == result

		where:
		input                           || result
		"scheme://host/path?query#frag" || true
		"mailto:user@example.com"       || true
		"abc:/path"                     || true
	}

	@Unroll
	def "NUMBER/ POSITIVE_NUMBER/ FLOAT_NUMBER/ POSITIVE_FLOAT_NUMBER: #input"() {
		expect:
		input.matches(RegExPool.NUMBER) == mNumber
		input.matches(RegExPool.POSITIVE_NUMBER) == mPosNumber
		input.matches(RegExPool.FLOAT_NUMBER) == mFloat
		input.matches(RegExPool.POSITIVE_FLOAT_NUMBER) == mPosFloat

		where:
		input  || mNumber | mPosNumber | mFloat | mPosFloat
		"0"    || true    | true       | false  | false
		"-10"  || true    | false      | false  | false
		"3.14" || false   | false      | true   | true
		"-0.5" || false   | false      | true   | false
		"abc"  || false   | false      | false  | false
	}

	@Unroll
	def "MIME_TYPE 匹配: #input -> #result"() {
		expect:
		input.matches(RegExPool.MIME_TYPE) == result

		where:
		input                             || result
		"text/plain"                      || true
		"application/json; charset=utf-8" || true
		"invalid/type;"                   || false
	}

	@Unroll
	def "IDENTIFIER 标识符匹配: #input -> #result"() {
		expect:
		input.matches(RegExPool.IDENTIFIER) == result

		where:
		input   || result
		"abc"   || true
		"_var1" || true
		"1abc"  || false
		"a-b"   || false
	}

	@Unroll
	def "MD5 匹配: #input -> #result"() {
		expect:
		input.matches(RegExPool.MD5) == result

		where:
		input                              || result
		"d41d8cd98f00b204e9800998ecf8427e" || true
		"D41D8CD98F00B204E9800998ECF8427E" || true
		"xyz"                              || false
		"d41d8cd98f00b204e9800998ecf8427"  || false
	}

	@Unroll
	def "BANK_CARD 银行卡号匹配: #input -> #result"() {
		expect:
		input.matches(RegExPool.BANK_CARD) == result

		where:
		input                 || result
		"6222021234567890123" || true
		"1234567890"          || true
		"0123456789"          || false
		"123456789"           || false
	}

	@Unroll
	def "FILENAME/FILENAME_WITHOUT_EXTENSION 匹配: #input -> #fname/#fnameNoExt"() {
		expect:
		input.matches(RegExPool.FILENAME) == fname
		input.matches(RegExPool.FILENAME_WITHOUT_EXTENSION) == fnameNoExt

		where:
		input        || fname | fnameNoExt
		"readme.txt" || true  | false
		"file"       || true  | true
		"a/b.txt"    || false | false
		"a<.txt"     || false | false
	}

	@Unroll
	def "UUID 系列匹配: #input -> #uuid/#uuidSimple/#javaUuid/#javaUuidSimple"() {
		expect:
		input.matches(RegExPool.UUID) == uuid
		input.matches(RegExPool.UUID_SIMPLE) == uuidSimple
		input.matches(RegExPool.JAVA_UUID) == javaUuid
		input.matches(RegExPool.JAVA_UUID_SIMPLE) == javaUuidSimple

		where:
		input                                  || uuid  | uuidSimple | javaUuid | javaUuidSimple
		"123e4567-e89b-12d3-a456-426614174000" || true  | false      | true     | false
		"123e4567e89b12d3a456426614174000"     || false | true       | false    | true
		"123E4567-E89B-12D3-A456-426614174000" || true  | false      | false    | false
	}

	@Unroll
	def "MAC/HEX 匹配: #input -> #mac/#hex"() {
		expect:
		input.matches(RegExPool.MAC) == mac
		input.matches(RegExPool.HEX) == hex

		where:
		input               || mac   | hex
		"a0:b1:c2:d3:e4:f5" || true  | false
		"a0-b1-c2-d3-e4-f5" || true  | false
		"DEADBEEF"          || false | true
		"G123"              || false | false
	}

	@Unroll
	def "DATE/TIME_12/TIME_24 匹配: #input -> #date/#t12/#t24"() {
		expect:
		input.matches(RegExPool.DATE) == date
		input.matches(RegExPool.TIME_12) == t12
		input.matches(RegExPool.TIME_24) == t24

		where:
		input        || date  | t12   | t24
		"2024-02-29" || true  | false | false
		"2023-02-30" || false | false | false
		"12:34:56"   || false | true  | true
		"00:00:00"   || false | false | true
		"24:00:00"   || false | false | false
	}

	@Unroll
	def "车辆/信用/网络编号匹配: #input -> #plate/#credit/#netmask/#vin/#drive"() {
		expect:
		input.matches(RegExPool.VEHICLE_PLATE_NUMBER) == plate
		input.matches(RegExPool.CREDIT_CODE) == credit
		input.matches(RegExPool.NET_MASK) == netmask
		input.matches(RegExPool.VEHICLE_FRAME_NUMBER) == vin
		input.matches(RegExPool.VEHICLE_DRIVING_NUMBER) == drive

		where:
		input                || plate | credit | netmask | vin   | drive
		"粤B12345"           || true  | false  | false   | false | false
		"91350211ABCD123456" || false | true   | false   | false | false
		"255.255.255.0"      || false | false  | true    | false | false
		"LDC613P23A1305189"  || false | false  | false   | true  | false
		"430101758218"       || false | false  | false   | false | true
	}

	@Unroll
	def "CHINESE_NAME/PHONE_IMEI 匹配: #input -> #name/#imei"() {
		expect:
		input.matches(RegExPool.CHINESE_NAME) == name
		input.matches(RegExPool.PHONE_IMEI) == imei

		where:
		input                     || name  | imei
		"张三"                    || true  | false
		"阿卜杜尼亚孜·毛力尼亚孜" || true  | false
		"李"                      || false | false
		"123456789012345"         || false | true
		"1234567890123456"        || false | true
		"12345678901234"          || false | false
	}

	@Unroll
	def "Linux/Windows 路径匹配: #input -> #ldir/#lfile/#wdir/#wfile"() {
		expect:
		input.matches(RegExPool.LINUX_DIR_PATH) == ldir
		input.matches(RegExPool.LINUX_FILE_PATH) == lfile
		input.matches(RegExPool.WINDOWS_DIR_PATH) == wdir
		input.matches(RegExPool.WINDOWS_FILE_PATH) == wfile

		where:
		input                            || ldir  | lfile | wdir  | wfile
		"/"                              || true  | false | false | false
		"/usr/local/bin/bash"            || false | true  | false | false
		"C:\\Windows\\"                  || false | false | true  | false
		"C:\\Windows\\System32\\cmd.exe" || false | false | true  | true
		"C:/Windows"                     || false | false | true  | true
	}

	@Unroll
	def "媒体链接匹配: #input -> #img/#video"() {
		expect:
		input.matches(RegExPool.IMAGE_URL) == img
		input.matches(RegExPool.VIDEO_URL) == video

		where:
		input                         || img   | video
		"https://example.com/a/b.jpg" || true  | false
		"https://example.com/a/b.mp4" || false | true
	}

	@Unroll
	def "PASSPORT/DOMAIN 匹配: #input -> #passport/#domain"() {
		expect:
		input.matches(RegExPool.PASSPORT) == passport
		input.matches(RegExPool.DOMAIN) == domain

		where:
		input               || passport | domain
		"E12345678"         || true     | false
		"G12345678"         || true     | false
		"example.com"       || false    | true
		"sub.example.co.uk" || false    | true
		"localhost"         || false    | false
	}
}

