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
		result.getLongitude().setScale(6, RoundingMode.HALF_UP) == new BigDecimal(expectedLng)
		result.getLatitude().setScale(6, RoundingMode.HALF_UP) == new BigDecimal(expectedLat)

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
		String dms = CoordinateUtils.toLongitudeDms(input.getLongitude())
		BigDecimal decimal = CoordinateUtils.fromDMS(dms)

		then:
		decimal.subtract(input.getLongitude()).abs() < new BigDecimal("0.00001")
	}

	// 测试误差范围
	def "测试GCJ02转换误差范围"() {
		given:
		Coordinate wgs84 = new Coordinate(116.3915, 39.9042)

		when:
		Coordinate gcj02 = CoordinateUtils.WGS84ToGCJ02(wgs84)
		Coordinate convertedBack = CoordinateUtils.GCJ02ToWGS84(gcj02)

		then:
		(convertedBack.getLongitude() - wgs84.getLongitude()).abs() < new BigDecimal("0.000833") // ~50米误差
		(convertedBack.getLatitude() - wgs84.getLatitude()).abs() < new BigDecimal("0.000833")
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
		String dms = CoordinateUtils.toLongitudeDms(coord.getLongitude())

		then:
		dms.contains("179°59'59.64\"E")
	}

	// 距离计算：同一点应为0
	def "测试距离计算 - 同一点为0米"() {
		given:
		Coordinate a = new Coordinate(0.0, 0.0)
		Coordinate b = new Coordinate(0.0, 0.0)

		expect:
		CoordinateUtils.calculateDistance(a, b) == 0.0d
	}

	// 距离计算：赤道附近纬度相差1度约110.6km
	def "测试距离计算 - 赤道纬度相差1度"() {
		given:
		Coordinate start = new Coordinate(0.0, 0.0)    // (lng, lat)
		Coordinate end = new Coordinate(0.0, 1.0)

		when:
		double distance = CoordinateUtils.calculateDistance(start, end)

		then:
		distance > 110_000 && distance < 111_000
	}

	// 距离计算：北京到上海直线距离合理范围
	def "测试距离计算 - 北京到上海"() {
		given:
		Coordinate beijing = new Coordinate(116.4074, 39.9042)
		Coordinate shanghai = new Coordinate(121.4737, 31.2304)

		when:
		double distance = CoordinateUtils.calculateDistance(beijing, shanghai)

		then:
		distance > 1_040_000 && distance < 1_120_000
	}

	// 面积计算：赤道附近 0.1° x 0.1° 矩形
	def "测试多边形面积计算 - 0.1度方形赤道附近"() {
		given:
		List<Coordinate> polygon = [
			new Coordinate(0.0, 0.0),
			new Coordinate(0.1, 0.0),
			new Coordinate(0.1, 0.1),
			new Coordinate(0.0, 0.1)
		]

		when:
		BigDecimal area = CoordinateUtils.calculateArea(polygon)

		then:
		area.doubleValue() > 100_000_000d && area.doubleValue() < 135_000_000d
	}

	// 面积计算：自动闭合与顺序无关（结果取绝对值）
	def "测试多边形面积计算 - 顺序与闭合"() {
		given:
		List<Coordinate> ccw = [
			new Coordinate(0.0, 0.0),
			new Coordinate(0.1, 0.0),
			new Coordinate(0.1, 0.1),
			new Coordinate(0.0, 0.1)
		]
		List<Coordinate> cw = [
			new Coordinate(0.0, 0.1),
			new Coordinate(0.1, 0.1),
			new Coordinate(0.1, 0.0),
			new Coordinate(0.0, 0.0)
		]

		expect:
		CoordinateUtils.calculateArea(ccw).setScale(0, RoundingMode.DOWN) ==
			CoordinateUtils.calculateArea(cw).setScale(0, RoundingMode.DOWN)
	}

	// 面积计算：非法输入（不足3个有效点）
	def "测试多边形面积计算 - 非法输入抛异常"() {
		given:
		List<Coordinate> invalid = [new Coordinate(0.0, 0.0), null]

		when:
		CoordinateUtils.calculateArea(invalid)

		then:
		thrown(IllegalArgumentException)
	}

	def "测试多边形周长计算 - 0.1度方形赤道附近"() {
		given:
		List<Coordinate> polygon = [
			new Coordinate(0.0, 0.0),
			new Coordinate(0.1, 0.0),
			new Coordinate(0.1, 0.1),
			new Coordinate(0.0, 0.1)
		]

		when:
		BigDecimal perimeter = CoordinateUtils.calculatePerimeter(polygon)

		then:
		perimeter.doubleValue() > 44_000d && perimeter.doubleValue() < 45_500d
	}

	def "测试多边形周长计算 - 顺序与闭合"() {
		given:
		List<Coordinate> ccw = [
			new Coordinate(0.0, 0.0),
			new Coordinate(0.1, 0.0),
			new Coordinate(0.1, 0.1),
			new Coordinate(0.0, 0.1)
		]
		List<Coordinate> notClosedCw = [
			new Coordinate(0.0, 0.1),
			new Coordinate(0.1, 0.1),
			new Coordinate(0.1, 0.0),
			new Coordinate(0.0, 0.0)
		]

		expect:
		CoordinateUtils.calculatePerimeter(ccw).setScale(0, RoundingMode.DOWN) ==
			CoordinateUtils.calculatePerimeter(notClosedCw).setScale(0, RoundingMode.DOWN)
	}

	def "测试多边形周长计算 - 非法输入抛异常"() {
		given:
		List<Coordinate> invalid = [new Coordinate(0.0, 0.0), null]

		when:
		CoordinateUtils.calculatePerimeter(invalid)

		then:
		thrown(IllegalArgumentException)
	}

	def "测试点在多边形内判定 - 简单矩形"() {
		given:
		List<Coordinate> rect = [
			new Coordinate(0.0, 0.0),
			new Coordinate(0.2, 0.0),
			new Coordinate(0.2, 0.2),
			new Coordinate(0.0, 0.2)
		]
		Coordinate inside = new Coordinate(0.1, 0.1)
		Coordinate outside = new Coordinate(0.3, 0.3)
		Coordinate boundary = new Coordinate(0.2, 0.1)

		expect:
		CoordinateUtils.isPointInPolygon(inside, rect)
		!CoordinateUtils.isPointInPolygon(outside, rect)
		CoordinateUtils.isPointInPolygon(boundary, rect)
	}

	def "测试点在多边形内判定 - 跨越180度经线"() {
		given:
		List<Coordinate> polygon = [
			new Coordinate(179.5, 10.0),
			new Coordinate(-179.5, 10.0),
			new Coordinate(-179.5, 11.0),
			new Coordinate(179.5, 11.0)
		]
		Coordinate inside = new Coordinate(179.9, 10.5)
		Coordinate outside = new Coordinate(179.9, 9.0)

		expect:
		CoordinateUtils.isPointInPolygon(inside, polygon)
		!CoordinateUtils.isPointInPolygon(outside, polygon)
	}

	def "测试点在多边形内判定 - 非法输入抛异常"() {
		given:
		List<Coordinate> invalidPolygon = [new Coordinate(0.0, 0.0), null]

		when:
		CoordinateUtils.isPointInPolygon(null, invalidPolygon)

		then:
		thrown(IllegalArgumentException)
	}
}
