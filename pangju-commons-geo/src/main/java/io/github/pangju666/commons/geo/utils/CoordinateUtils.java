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

package io.github.pangju666.commons.geo.utils;

import ch.obermuhlner.math.big.BigDecimalMath;
import io.github.pangju666.commons.geo.lang.GeoConstants;
import io.github.pangju666.commons.geo.model.Coordinate;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 地理坐标转换工具类
 * <p>提供坐标系转换、度分秒格式处理等核心功能，支持高精度地理计算</p>
 *
 * <h3>主要功能增强：</h3>
 * <ul>
 *     <li><strong>经纬度分离处理</strong> - 新增纬度/经度专用转换方法</li>
 *     <li><strong>输入验证增强</strong> - 自动过滤无效坐标值</li>
 *     <li><strong>国际化支持</strong> - 使用标准方向字符常量</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class CoordinateUtils {
	/**
	 * 圆周率
	 *
	 * @since 1.0.0
	 */
	protected static final BigDecimal PI = BigDecimal.valueOf(3.1415926535897932384626);
	/**
	 * 长半轴
	 *
	 * @since 1.0.0
	 */
	protected static final BigDecimal A = BigDecimal.valueOf(6378245.0);
	/**
	 * 偏心率平方
	 *
	 * @since 1.0.0
	 */
	protected static final BigDecimal EE = BigDecimal.valueOf(0.00669342162296594323);
	/**
	 * 经纬度度分秒格式
	 *
	 * @since 1.0.0
	 */
	protected static final String DMS_FORMAT = "%s" + GeoConstants.RADIUS_CHAR + "%s" +
		GeoConstants.MINUTE_CHAR + "%.2f" + GeoConstants.SECONDS_CHAR + "%c";

	protected static final BigDecimal ZERO_ONE = BigDecimal.valueOf(0.1);
	protected static final BigDecimal ZERO_TWO = BigDecimal.valueOf(0.2);
	protected static final BigDecimal TWO = BigDecimal.valueOf(2);
	protected static final BigDecimal THREE = BigDecimal.valueOf(3);
	protected static final BigDecimal SIX = BigDecimal.valueOf(6);
	protected static final BigDecimal TWELVE = BigDecimal.valueOf(12);
	protected static final BigDecimal TWENTY = BigDecimal.valueOf(20);
	protected static final BigDecimal THIRTY = BigDecimal.valueOf(30);
	protected static final BigDecimal THIRTY_FIVE = BigDecimal.valueOf(35);
	protected static final BigDecimal FORTY = BigDecimal.valueOf(40);
	protected static final BigDecimal SIXTY = BigDecimal.valueOf(60);
	protected static final BigDecimal NEGATE_ONE_HUNDRED = BigDecimal.valueOf(-100);
	protected static final BigDecimal ONE_HUNDRED_AND_FIVE = BigDecimal.valueOf(105);
	protected static final BigDecimal ONE_HUNDRED_AND_FIFTY = BigDecimal.valueOf(150);
	protected static final BigDecimal ONE_HUNDRED_AND_SIXTY = BigDecimal.valueOf(160);
	protected static final BigDecimal ONE_HUNDRED_AND_EIGHTY = BigDecimal.valueOf(180);
	protected static final BigDecimal THREE_HUNDRED = BigDecimal.valueOf(300);
	protected static final BigDecimal THREE_HUNDRED_AND_TWENTY = BigDecimal.valueOf(320);
	protected static final BigDecimal THREE_THOUSAND_AND_SIX_HUNDRED = BigDecimal.valueOf(3600);

	protected CoordinateUtils() {
	}

	/**
	 * 将十进制纬度转换为度分秒格式
	 *
	 * @param coordinate 十进制纬度值（范围：-90° ~ 90°）
	 * @return 度分秒格式纬度字符串（示例：39°54'15.12"N），超出范围返回null
	 * @since 1.0.0
	 */
	public static String toLatitudeDMS(final BigDecimal coordinate) {
		if (Objects.isNull(coordinate)) {
			return null;
		}
		if (coordinate.doubleValue() > GeoConstants.MAX_LATITUDE || coordinate.doubleValue() < GeoConstants.MIN_LATITUDE) {
			return null;
		}
		return toDMS(coordinate, true);
	}

	/**
	 * 将十进制经度转换为度分秒格式
	 *
	 * @param coordinate 十进制经度值（范围：-180° ~ 180°）
	 * @return 度分秒格式经度字符串（示例：116°23'29.34"E），超出范围返回null
	 * @since 1.0.0
	 */
	public static String toLongitudeDms(final BigDecimal coordinate) {
		if (Objects.isNull(coordinate)) {
			return null;
		}
		if (coordinate.doubleValue() > GeoConstants.MAX_LONGITUDE || coordinate.doubleValue() < GeoConstants.MIN_LONGITUDE) {
			return null;
		}
		return toDMS(coordinate, false);
	}

	/**
	 * 将度分秒格式经纬度转换为十进制度经纬度
	 *
	 * @param dms 度分秒格式经纬度（示例：116°23'29.34"E）
	 * @return 十进制经纬度，方向为西/南时返回负数。输入空值返回null
	 * @throws NumberFormatException    当数值解析失败时抛出
	 * @since 1.0.0
	 */
	public static BigDecimal fromDMS(final String dms) {
		if (StringUtils.isBlank(dms)) {
			return null;
		}

		try {
			int degreeIndex = dms.indexOf(GeoConstants.RADIUS_CHAR);
			BigDecimal degreeNumber = new BigDecimal(dms.substring(0, degreeIndex));

			int minuteIndex = dms.indexOf(GeoConstants.MINUTE_CHAR, degreeIndex);
			BigDecimal minuteNumber = new BigDecimal(dms.substring(degreeIndex + 1, minuteIndex));

			int secondsIndex = dms.indexOf(GeoConstants.SECONDS_CHAR, minuteIndex);
			BigDecimal secondsNumber = new BigDecimal(dms.substring(minuteIndex + 1, secondsIndex));

			boolean negative = false;
			char direction = dms.charAt(dms.length() - 1);
			if (direction == GeoConstants.WEST_CHAR || direction == GeoConstants.SOUTH_CHAR) {
				negative = true;
			}

			BigDecimal coordinate = degreeNumber.add(minuteNumber.divide(SIXTY, MathContext.DECIMAL32))
				.add(secondsNumber.divide(THREE_THOUSAND_AND_SIX_HUNDRED, MathContext.DECIMAL32));
			if (negative) {
				coordinate = coordinate.negate();
			}
			return coordinate;
		} catch (StringIndexOutOfBoundsException e) {
			throw new NumberFormatException("无效的度分秒格式经纬度");
		}
	}

	/**
	 * GCJ-02坐标系转WGS-84坐标系
	 *
	 * @param coordinate GCJ-02坐标（必须为中国境内坐标）
	 * @return WGS-84坐标系坐标，输入空值返回null
	 * @apiNote 转换存在约50-500米误差
	 * @since 1.0.0
	 */
	public static Coordinate GCJ02ToWGS84(final Coordinate coordinate) {
		if (Objects.isNull(coordinate)) {
			return null;
		}

		if (coordinate.isOutOfChina()) {
			return coordinate;
		} else {
			Coordinate deltaCoordinate = computeGcj02Delta(coordinate);
			return new Coordinate(coordinate.longitude().subtract(deltaCoordinate.longitude()),
				coordinate.latitude().subtract(deltaCoordinate.latitude()));
		}
	}

	/**
	 * WGS-84坐标系转GCJ-02坐标系
	 *
	 * @param coordinate WGS-84坐标（必须为中国境内坐标）
	 * @return GCJ-02坐标系坐标，输入空值返回null
	 * @apiNote 中国境外坐标直接返回原值
	 * @since 1.0.0
	 */
	public static Coordinate WGS84ToGCJ02(final Coordinate coordinate) {
		if (Objects.isNull(coordinate)) {
			return null;
		}

		if (coordinate.isOutOfChina()) {
			return coordinate;
		} else {
			Coordinate deltaCoordinate = computeGcj02Delta(coordinate);
			return new Coordinate(coordinate.longitude().add(deltaCoordinate.longitude()),
				coordinate.latitude().add(deltaCoordinate.latitude()));
		}
	}

	/**
	 * 计算GCJ-02坐标系偏移量
	 *
	 * @param coordinate 原始坐标
	 * @return 经/纬度偏移量
	 * @apiNote 网上找到的实现
	 * @since 1.0.0
	 */
	protected static Coordinate computeGcj02Delta(final Coordinate coordinate) {
		// latitude / 180.0 * PI
		BigDecimal radiusLatitude = coordinate.latitude().divide(ONE_HUNDRED_AND_EIGHTY, MathContext.DECIMAL32)
			.multiply(PI);
		// 1 - EE * (sin(radiusLatitude))²
		BigDecimal magic = BigDecimal.ONE.subtract(EE.multiply(BigDecimalMath.sin(
			radiusLatitude, MathContext.DECIMAL32).pow(2)));
		// sqrt(magic)
		BigDecimal sqrtMagic = magic.sqrt(MathContext.DECIMAL32);

		// (transformLat(longitude - 105.0, latitude - 35.0) * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI)
		BigDecimal deltaLatitude = transformLatitude(coordinate.longitude().subtract(ONE_HUNDRED_AND_FIVE),
			coordinate.latitude().subtract(THIRTY_FIVE)).multiply(ONE_HUNDRED_AND_EIGHTY)
			.divide(A.multiply(BigDecimal.ONE.subtract(EE))
				.divide(magic.multiply(sqrtMagic), MathContext.DECIMAL32)
				.multiply(PI), MathContext.DECIMAL32);

		// (transformLat(longitude - 105.0, latitude - 35.0) * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI)
		BigDecimal deltaLongitude = transformLongitude(coordinate.longitude().subtract(ONE_HUNDRED_AND_FIVE),
			coordinate.latitude().subtract(THIRTY_FIVE)).multiply(ONE_HUNDRED_AND_EIGHTY)
			.divide(A.divide(sqrtMagic, MathContext.DECIMAL32)
				.multiply(BigDecimalMath.cos(radiusLatitude, MathContext.DECIMAL32))
				.multiply(PI), MathContext.DECIMAL32);

		return new Coordinate(deltaLongitude, deltaLatitude);
	}

	/**
	 * 经度偏移变换计算
	 *
	 * @param longitude 经度偏移基数（已减105.0）
	 * @param latitude  纬度偏移基数（已减35.0）
	 * @return 经度偏移值
	 * @since 1.0.0
	 */
	protected static BigDecimal transformLongitude(final BigDecimal longitude, final BigDecimal latitude) {
		return longitude
			// + 300
			.add(THREE_HUNDRED)
			// + (2.0 * latitude)
			.add(latitude.multiply(TWO))
			// + (0.1 * longitude * longitude)
			.add(longitude.pow(2).multiply(ZERO_ONE))
			// + (0.1 * longitude * latitude)
			.add(longitude.multiply(ZERO_ONE).multiply(latitude))
			// + (0.1 * Math.sqrt(Math.abs(longitude)))
			.add(longitude.abs().sqrt(MathContext.DECIMAL32).multiply(ZERO_ONE))
			// ((20.0 * Math.sin(6.0 * longitude * PI) + 20.0 * Math.sin(2.0 * longitude * PI)) * 2.0 / 3.0)
			.add(BigDecimalMath
				.sin(longitude.multiply(SIX).multiply(PI),
					MathContext.DECIMAL32)
				.multiply(TWENTY)
				.add(BigDecimalMath.sin(longitude.multiply(TWO).multiply(PI),
					MathContext.DECIMAL32).multiply(TWENTY))
				.multiply(TWO)
				.divide(THREE, MathContext.DECIMAL32))
			// ((20.0 * Math.sin(longitude * PI) + 40.0 * Math.sin((lng / 3.0) * PI)) * 2.0 / 3.0)
			.add(BigDecimalMath
				.sin(longitude.multiply(PI), MathContext.DECIMAL32)
				.multiply(TWENTY)
				.add(BigDecimalMath
					.sin(longitude.divide(THREE, MathContext.DECIMAL32)
						.multiply(PI), MathContext.DECIMAL32)
					.multiply(FORTY))
				.multiply(TWO)
				.divide(THREE, MathContext.DECIMAL32))
			// ((150.0 * Math.sin((lng / 12.0) * PI) + // 300.0 * Math.sin((lng / 30.0) * PI)) * 2.0 / 3.0)
			.add(BigDecimalMath
				.sin(longitude.divide(TWELVE, MathContext.DECIMAL32).multiply(PI),
					MathContext.DECIMAL32)
				.multiply(ONE_HUNDRED_AND_FIFTY)
				.add(BigDecimalMath
					.sin(longitude.divide(THIRTY, MathContext.DECIMAL32).multiply(PI),
						MathContext.DECIMAL32)
					.multiply(THREE_HUNDRED))
				.multiply(TWO)
				.divide(THREE, MathContext.DECIMAL32));
	}

	/**
	 * 纬度偏移变换计算（内部算法）
	 *
	 * @param longitude 经度偏移基数（已减105.0）
	 * @param latitude  纬度偏移基数（已减35.0）
	 * @return 纬度偏移值
	 * @since 1.0.0
	 */
	protected static BigDecimal transformLatitude(final BigDecimal longitude, final BigDecimal latitude) {
		return longitude
			// * 2.0
			.multiply(TWO)
			// -100
			.add(NEGATE_ONE_HUNDRED)
			// + (latitude * 3.0)
			.add(latitude.multiply(THREE))
			// + (0.2 * latitude * latitude)
			.add(latitude.pow(2).multiply(ZERO_TWO))
			// + (0.1 * longitude * latitude)
			.add(longitude.multiply(ZERO_ONE).multiply(latitude))
			// + (0.2 * Math.sqrt(Math.abs(longitude)))
			.add(longitude.abs().sqrt(MathContext.DECIMAL32).multiply(ZERO_TWO))
			// + ((20.0 * Math.sin(6.0 * longitude * PI) + 20.0 * Math.sin(2.0 * longitude * PI)) * 2.0 / 3.0)
			.add(BigDecimalMath
				.sin(longitude.multiply(SIX).multiply(PI),
					MathContext.DECIMAL32)
				.multiply(TWENTY)
				.add(BigDecimalMath.sin(longitude.multiply(TWO).multiply(PI),
						MathContext.DECIMAL32)
					.multiply(TWENTY))
				.multiply(TWO)
				.divide(THREE, MathContext.DECIMAL32))
			// + ((20.0 * Math.sin(latitude * PI) + 40.0 * Math.sin(latitude / 3.0) * PI)) * 2.0 / 3.0)
			.add(BigDecimalMath.sin(latitude.multiply(PI), MathContext.DECIMAL32)
				.multiply(TWENTY)
				.add(BigDecimalMath
					.sin(latitude.divide(THREE, MathContext.DECIMAL32)
						.multiply(PI), MathContext.DECIMAL32)
					.multiply(FORTY))
				.multiply(TWO)
				.divide(THREE, MathContext.DECIMAL32))
			// ((160.0 * Math.sin((latitude / 12.0 * PI) + 320 * Math.sin(latitude * PI / 30.0)) * 2.0 / 3.0)
			.add(BigDecimalMath.sin(latitude.divide(TWELVE, MathContext.DECIMAL32)
						.multiply(PI),
					MathContext.DECIMAL32).multiply(ONE_HUNDRED_AND_SIXTY)
				.add(BigDecimalMath.sin(latitude.multiply(PI)
						.divide(THIRTY, MathContext.DECIMAL32),
					MathContext.DECIMAL32).multiply(THREE_HUNDRED_AND_TWENTY))
				.multiply(TWO)
				.divide(THREE, MathContext.DECIMAL32));
	}

	/**
	 * 将十进制纬度转换为度分秒格式
	 *
	 * @param coordinate 十进制度数值
	 * @param latitude   坐标类型标记：true表示纬度，false表示经度
	 * @return 格式化的度分秒字符串
	 * @implNote 方向字符根据坐标类型和正负值自动确定：
	 * <ul>
	 *     <li>纬度：N(北纬)/S(南纬)</li>
	 *     <li>经度：E(东经)/W(西经)</li>
	 * </ul>
	 * @since 1.0.0
	 */
	protected static String toDMS(BigDecimal coordinate, boolean latitude) {
		// 确保输入值为正数以方便计算
		boolean isNegative = coordinate.signum() < 0;
		BigDecimal absDecimalDegree = coordinate.abs();

		// 计算度、分和秒
		BigDecimal degrees = absDecimalDegree.setScale(0, RoundingMode.DOWN); // 取整得到度
		BigDecimal remaining = absDecimalDegree.subtract(degrees);            // 剩余的小数部分
		BigDecimal minutes = remaining.multiply(SIXTY).setScale(0, RoundingMode.DOWN); // 计算分
		BigDecimal seconds = remaining.multiply(SIXTY)
			.subtract(minutes)
			.multiply(SIXTY)
			.setScale(2, RoundingMode.HALF_UP); // 计算秒，保留两位小数

		// 判断方向
		char direction = isNegative ? (latitude ? GeoConstants.SOUTH_CHAR : GeoConstants.WEST_CHAR) :
			(latitude ? GeoConstants.North_DIRECTION : GeoConstants.EAST_CHAR);

		// 格式化输出
		return String.format(DMS_FORMAT, degrees.toPlainString(), minutes.toPlainString(), seconds.doubleValue(), direction);
	}
}
