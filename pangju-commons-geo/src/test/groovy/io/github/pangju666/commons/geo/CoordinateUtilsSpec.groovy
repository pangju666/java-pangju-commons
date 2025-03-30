package io.github.pangju666.commons.geo

import io.github.pangju666.commons.geo.model.Coordinate
import io.github.pangju666.commons.geo.utils.CoordinateUtils
import spock.lang.Specification
import spock.lang.Unroll

import java.math.RoundingMode

class CoordinateUtilsSpec extends Specification {
	// 测试toDMS方法
	@Unroll
	def "测试toDMS转换 - #decimal° 应转换为 #expected"() {
		given:
		BigDecimal input = new BigDecimal(decimal)

		when:
		String result = latitude ? CoordinateUtils.toLatitudeDMS(input) : CoordinateUtils.toLongitudeDms(input)

		then:
		result == expected

		where:
		decimal    | expected          | latitude
		"116.3915" | "116°23'29.40\"E" | false
		"39.9042"  | "39°54'15.12\"N"  | true
		"-79.3832" | "79°22'59.52\"S"  | true
		"0.0"      | "0°0'0.00\"N"     | true
		"90.0"     | "90°0'0.00\"N"    | true
		"-90.0"    | "90°0'0.00\"S"    | true
		"181.0"    | null              | false // 测试边界外值
	}

	// 测试fromDMS方法
	@Unroll
	def "测试fromDMS转换 - #dms 应转换为 #expected"() {
		when:
		BigDecimal result = CoordinateUtils.fromDMS(dms)

		then:
		result.setScale(4, RoundingMode.HALF_UP) == new BigDecimal(expected)

		where:
		dms               | expected
		"116°23'29.40\"E" | "116.3915"
		"39°54'15.12\"N"  | "39.9042"
		"79°22'59.52\"W"  | "-79.3832"
		"0°0'0.00\"N"     | "0.0000"
		"90°0'0.00\"S"    | "-90.0000"
	}

	// 测试坐标系转换
	@Unroll
	def "测试坐标系转换 - #scenario"() {
		given:
		Coordinate input = new Coordinate(lng, lat)

		when:
		Coordinate result = CoordinateUtils."$method"(input)

		then:
		result.longitude().setScale(6, RoundingMode.HALF_UP) == new BigDecimal(expectedLng)
		result.latitude().setScale(6, RoundingMode.HALF_UP) == new BigDecimal(expectedLat)

		where:
		scenario                      | method         | lng       | lat     | expectedLng  | expectedLat
		"GCJ02转WGS84中国境内"        | "GCJ02ToWGS84" | 116.3915  | 39.9042 | "116.385259" | "39.902799"
		"WGS84转GCJ02中国境内"        | "WGS84ToGCJ02" | 116.3915  | 39.9042 | "116.397741" | "39.905601"
		"境外坐标不转换-GCJ02转WGS84" | "GCJ02ToWGS84" | 139.6917  | 35.6895 | "139.6917"   | "35.6895"
		"境外坐标不转换-WGS84转GCJ02" | "WGS84ToGCJ02" | -122.4194 | 37.7749 | "-122.4194"  | "37.7749"
	}

	def "测试fromDMS传入非法格式抛出异常"() {
		when:
		CoordinateUtils.fromDMS("invalid format")

		then:
		thrown(NumberFormatException)
	}

	// 测试边界条件
	def "测试中国边界判断"() {
		given:
		Coordinate chinaCoord = new Coordinate(116.4074, 39.9042)
		Coordinate foreignCoord = new Coordinate(139.6917, 35.6895)

		expect:
		!chinaCoord.isOutOfChina()
		foreignCoord.isOutOfChina()
	}

	// 测试大数计算精度
	def "测试高精度计算"() {
		given:
		Coordinate input = new Coordinate(116.39151234, 39.90421234)

		when:
		String dms = CoordinateUtils.toLongitudeDms(input.longitude())
		BigDecimal decimal = CoordinateUtils.fromDMS(dms)

		then:
		decimal.subtract(input.longitude()).abs() < new BigDecimal("0.00001")
	}

	// 测试误差范围
	def "测试GCJ02转换误差范围"() {
		given:
		Coordinate wgs84 = new Coordinate(116.3915, 39.9042)

		when:
		Coordinate gcj02 = CoordinateUtils.WGS84ToGCJ02(wgs84)
		Coordinate convertedBack = CoordinateUtils.GCJ02ToWGS84(gcj02)

		then:
		(convertedBack.longitude() - wgs84.longitude()).abs() < new BigDecimal("0.000833") // ~50米误差
		(convertedBack.latitude() - wgs84.latitude()).abs() < new BigDecimal("0.000833")
	}

	// 辅助方法测试（需要反射访问protected方法）
	def "测试transformLongitude计算"() {
		given:
		BigDecimal lng = new BigDecimal("11.3915")
		BigDecimal lat = new BigDecimal("28.9042")

		when:
		def method = CoordinateUtils.getDeclaredMethod("transformLongitude", BigDecimal, BigDecimal)
		method.setAccessible(true)
		BigDecimal result = method.invoke(null, lng, lat)

		then:
		result.abs() > BigDecimal.ZERO
	}

	// 性能测试
	def "测试批量转换性能"() {
		given:
		List<Coordinate> coordinates = (1..1000).collect {
			new Coordinate(116 + it / 10000.0, 39 + it / 10000.0)
		}

		when:
		def start = System.currentTimeMillis()
		coordinates.each { CoordinateUtils.WGS84ToGCJ02(it) }
		def duration = System.currentTimeMillis() - start

		then:
		duration < 1000 // 1秒内完成1000次转换
	}

// 异常格式测试
	@Unroll
	def "测试非法DMS格式：#invalidDms"() {
		when:
		CoordinateUtils.fromDMS(invalidDms)

		then:
		thrown(NumberFormatException)

		where:
		invalidDms << [
			"116°23'A29.40\"",          // 缺少方向
			"°23'29.40\"E",            // 缺少度数
			"116°'29.40\"E",           // 缺少分数
			"116°23'.40A\"E",           // 缺少秒数
			"ABC°23'29.40\"E"          // 非数字
		]
	}

// 国际日期变更线测试
	def "测试国际日期变更线附近坐标"() {
		given:
		Coordinate coord = new Coordinate(179.9999, 45.0)

		when:
		String dms = CoordinateUtils.toLongitudeDms(coord.longitude())

		then:
		dms.contains("179°59'59.64\"E")
	}
}
