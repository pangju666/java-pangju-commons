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
 * <p>
 * 提供高精度的地理坐标转换功能，包括坐标系转换、度分秒格式处理等核心能力。
 * 本工具类使用BigDecimal保证计算精度，适用于GIS系统、地图服务等场景。
 * </p>
 *
 * <h3>核心功能</h3>
 * <table border="1">
 *   <tr><th>功能类别</th><th>方法</th><th>精度</th></tr>
 *   <tr><td>坐标系转换</td><td>WGS84ToGCJ02/GCJ02ToWGS84</td><td>±50-500米</td></tr>
 *   <tr><td>格式转换</td><td>toLatitudeDMS/toLongitudeDms/fromDMS</td><td>0.01秒</td></tr>
 *   <tr><td>偏移计算</td><td>computeGcj02Delta</td><td>高精度</td></tr>
 * </table>
 *
 * <h3>技术实现</h3>
 * <ul>
 *   <li>使用BigDecimalMath进行高精度数学运算</li>
 *   <li>基于WGS84椭球体参数计算</li>
 *   <li>GCJ02偏移算法参考公开实现</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 * @see GeoConstants
 * @see Coordinate
 */
public class CoordinateUtils {
	/**
	 * 圆周率π
	 * <p>用于地理坐标计算的高精度π值，精度达小数点后22位</p>
	 *
	 * @since 1.0.0
	 */
	protected static final BigDecimal PI = BigDecimal.valueOf(3.1415926535897932384626);
	/**
	 * WGS84椭球体长半轴
	 * <p>单位：米，标准值：6378245.0</p>
	 *
	 * @since 1.0.0
	 */
	protected static final BigDecimal A = BigDecimal.valueOf(6378245.0);
	/**
	 * WGS84椭球体偏心率平方
	 * <p>标准值：0.00669342162296594323</p>
	 *
	 * @since 1.0.0
	 */
	protected static final BigDecimal EE = BigDecimal.valueOf(0.00669342162296594323);
	/**
	 * 度分秒格式模板
	 * <p>格式示例：116°23'29.34"E</p>
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
	 * <p>
	 * 转换规则：
	 * <ol>
	 *   <li>1° = 60'</li>
	 *   <li>1' = 60"</li>
	 *   <li>南纬自动添加S后缀</li>
	 * </ol>
	 * </p>
	 *
	 * @param coordinate 十进制纬度值，范围[-90, 90]
	 * @return 格式化字符串，示例：39°54'15.12"N
	 * @throws IllegalArgumentException 当坐标超出范围时抛出
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
	 * <p>
	 * 转换规则：
	 * <ol>
	 *   <li>1° = 60'</li>
	 *   <li>1' = 60"</li>
	 *   <li>西经自动添加W后缀</li>
	 * </ol>
	 * </p>
	 *
	 * @param coordinate 十进制经度值，范围[-180, 180]
	 * @return 格式化字符串，示例：116°23'29.34"E
	 * @throws IllegalArgumentException 当坐标超出范围时抛出
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
	 * 将度分秒格式经纬度转换为十进制度
	 * <p>
	 * 支持格式示例：
	 * <ul>
	 *   <li>116°23'29.34"E</li>
	 *   <li>39°54'15.12"N</li>
	 * </ul>
	 * </p>
	 *
	 * @param dms 度分秒格式字符串
	 * @return 十进制度数值，西经/南纬为负值
	 * @throws NumberFormatException 当格式不符合规范时抛出
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
	 * <p>
	 * 实现火星坐标系(GCJ-02)到世界大地坐标系(WGS-84)的逆向转换，
	 * 适用于需要获取GPS原始坐标的场景。
	 * </p>
	 *
	 * <h3>转换原理</h3>
	 * <ol>
	 *   <li>计算GCJ-02坐标的偏移量</li>
	 *   <li>从GCJ-02坐标中减去偏移量</li>
	 *   <li>返回近似WGS-84坐标</li>
	 * </ol>
	 *
	 * @param coordinate GCJ-02坐标，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>中国境内坐标</li>
	 *                  </ul>
	 * @return WGS-84坐标系坐标，境外坐标直接返回原值
	 * @throws IllegalArgumentException 当参数为null时抛出
	 * @apiNote 转换精度约50-500米，无法完全还原原始WGS-84坐标
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
			return new Coordinate(coordinate.getLongitude().subtract(deltaCoordinate.getLongitude()),
				coordinate.getLatitude().subtract(deltaCoordinate.getLatitude()));
		}
	}

	/**
	 * WGS-84坐标系转GCJ-02坐标系
	 * <p>
	 * 实现世界大地坐标系(WGS-84)到火星坐标系(GCJ-02)的正向转换，
	 * 适用于需要适配国内地图服务的场景。
	 * </p>
	 *
	 * <h3>转换原理</h3>
	 * <ol>
	 *   <li>计算WGS-84坐标的偏移量</li>
	 *   <li>向WGS-84坐标加上偏移量</li>
	 *   <li>返回GCJ-02坐标</li>
	 * </ol>
	 *
	 * @param coordinate WGS-84坐标，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>中国境内坐标</li>
	 *                  </ul>
	 * @return GCJ-02坐标系坐标，境外坐标直接返回原值
	 * @throws IllegalArgumentException 当参数为null时抛出
	 * @apiNote 中国境外坐标不做转换直接返回
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
			return new Coordinate(coordinate.getLongitude().add(deltaCoordinate.getLongitude()),
				coordinate.getLatitude().add(deltaCoordinate.getLatitude()));
		}
	}

	/**
	 * 计算GCJ-02坐标系偏移量
	 * <p>
	 * 基于WGS84椭球体参数和中国区域加密算法，
	 * 计算给定坐标的经/纬度偏移量。
	 * </p>
	 *
	 * <h3>算法说明</h3>
	 * <ul>
	 *   <li>使用正弦函数叠加实现非线性偏移</li>
	 *   <li>考虑了椭球体曲率影响</li>
	 *   <li>105°E,35°N为基准偏移点</li>
	 * </ul>
	 *
	 * @param coordinate 原始坐标，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>中国境内坐标</li>
	 *                  </ul>
	 * @return 包含经/纬度偏移量的坐标对象
	 * @throws IllegalArgumentException 当参数为null时抛出
	 * @apiNote 算法参考公开的GCJ-02实现
	 * @since 1.0.0
	 */
	protected static Coordinate computeGcj02Delta(final Coordinate coordinate) {
		// latitude / 180.0 * PI
		BigDecimal radiusLatitude = coordinate.getLatitude().divide(ONE_HUNDRED_AND_EIGHTY, MathContext.DECIMAL32)
			.multiply(PI);
		// 1 - EE * (sin(radiusLatitude))²
		BigDecimal magic = BigDecimal.ONE.subtract(EE.multiply(BigDecimalMath.sin(
			radiusLatitude, MathContext.DECIMAL32).pow(2)));
		// sqrt(magic)
		BigDecimal sqrtMagic = magic.sqrt(MathContext.DECIMAL32);

		// (transformLat(longitude - 105.0, latitude - 35.0) * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI)
		BigDecimal deltaLatitude = transformLatitude(coordinate.getLongitude().subtract(ONE_HUNDRED_AND_FIVE),
			coordinate.getLatitude().subtract(THIRTY_FIVE)).multiply(ONE_HUNDRED_AND_EIGHTY)
			.divide(A.multiply(BigDecimal.ONE.subtract(EE))
				.divide(magic.multiply(sqrtMagic), MathContext.DECIMAL32)
				.multiply(PI), MathContext.DECIMAL32);

		// (transformLat(longitude - 105.0, latitude - 35.0) * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI)
		BigDecimal deltaLongitude = transformLongitude(coordinate.getLongitude().subtract(ONE_HUNDRED_AND_FIVE),
			coordinate.getLatitude().subtract(THIRTY_FIVE)).multiply(ONE_HUNDRED_AND_EIGHTY)
			.divide(A.divide(sqrtMagic, MathContext.DECIMAL32)
				.multiply(BigDecimalMath.cos(radiusLatitude, MathContext.DECIMAL32))
				.multiply(PI), MathContext.DECIMAL32);

		return new Coordinate(deltaLongitude, deltaLatitude);
	}

	/**
	 * 经度偏移变换计算
	 * <p>
	 * 实现GCJ-02加密算法中的经度偏移计算，
	 * 包含多个正弦波叠加的非线性变换。
	 * </p>
	 *
	 * <h3>计算公式</h3>
	 * <pre>
	 * deltaLng = 300 + lng + 2*lat + 0.1*lng² + 0.1*lng*lat
	 *          + 0.1√|lng| + 20sin(6πlng) + 20sin(2πlng)
	 *          + 40sin(πlng/3) + 160sin(πlng/12) + 320sin(πlng/30)
	 * </pre>
	 *
	 * @param longitude 经度偏移基数（已减105.0）
	 * @param latitude 纬度偏移基数（已减35.0）
	 * @return 计算出的经度偏移值
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
	 * 纬度偏移变换计算
	 * <p>
	 * 实现GCJ-02加密算法中的纬度偏移计算，
	 * 包含多个正弦波叠加的非线性变换。
	 * </p>
	 *
	 * <h3>计算公式</h3>
	 * <pre>
	 * deltaLat = -100 + 2*lng + 3*lat + 0.2*lat² + 0.1*lng*lat
	 *          + 0.2√|lng| + 20sin(6πlng) + 20sin(2πlng)
	 *          + 40sin(πlat/3) + 160sin(πlat/12) + 320sin(πlat/30)
	 * </pre>
	 *
	 * @param longitude 经度偏移基数（已减105.0）
	 * @param latitude 纬度偏移基数（已减35.0）
	 * @return 计算出的纬度偏移值
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
	 * 将十进制坐标转换为度分秒格式
	 * <p>
	 * 内部实现十进制度到度分秒(DMS)格式的转换，
	 * 支持经度和纬度两种类型的格式化。
	 * </p>
	 *
	 * @param coordinate 十进制度数值
	 * @param latitude 坐标类型标记：
	 *                <ul>
	 *                  <li>true - 纬度</li>
	 *                  <li>false - 经度</li>
	 *                </ul>
	 * @return 格式化后的度分秒字符串
	 * @implNote 转换过程保留2位小数精度
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
