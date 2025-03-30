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

package io.github.pangju666.commons.geo.model;

import io.github.pangju666.commons.geo.lang.GeoConstants;
import io.github.pangju666.commons.geo.utils.CoordinateUtils;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;

/**
 * 地理坐标模型类
 * <p>表示一个地理坐标点，提供坐标验证、格式转换和位置判断功能</p>
 *
 * <h3>特性说明：</h3>
 * <ul>
 *     <li><strong>不可变对象</strong> - 保证线程安全</li>
 *     <li><strong>精确计算</strong> - 使用BigDecimal保持高精度</li>
 *     <li><strong>自动验证</strong> - 构造时自动校验坐标有效性</li>
 *     <li><strong>格式支持</strong> - 支持十进制度、度分秒格式转换</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public record Coordinate(BigDecimal longitude, BigDecimal latitude) {
	/**
	 * 主构造方法
	 *
	 * @param longitude 经度（必须满足：-180 ≤ longitude ≤ 180）
	 * @param latitude  纬度（必须满足：-90 ≤ latitude ≤ 90）
	 * @throws IllegalArgumentException 当参数不符合以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>经度/纬度为null</li>
	 *                                      <li>坐标值超出合法范围</li>
	 *                                  </ul>
	 * @since 1.0.0
	 */
	public Coordinate {
		Validate.notNull(longitude, "longitude 不可为null");
		Validate.notNull(latitude, "latitude 不可为null");
		Validate.inclusiveBetween(GeoConstants.MIN_LONGITUDE, GeoConstants.MAX_LONGITUDE, longitude.doubleValue());
		Validate.inclusiveBetween(GeoConstants.MIN_LATITUDE, GeoConstants.MAX_LATITUDE, latitude.doubleValue());
	}

	/**
	 * 双精度坐标构造方法
	 *
	 * @param longitude 经度（十进制度数）
	 * @param latitude  纬度（十进制度数）
	 * @see #Coordinate(BigDecimal, BigDecimal)
	 * @since 1.0.0
	 */
	public Coordinate(double longitude, double latitude) {
		this(BigDecimal.valueOf(longitude), BigDecimal.valueOf(latitude));
	}

	/**
	 * 单精度坐标构造方法
	 *
	 * @param longitude 经度（十进制度数）
	 * @param latitude  纬度（十进制度数）
	 * @see #Coordinate(BigDecimal, BigDecimal)
	 * @since 1.0.0
	 */
	public Coordinate(float longitude, float latitude) {
		this(BigDecimal.valueOf(longitude), BigDecimal.valueOf(latitude));
	}

	/**
	 * 长整数坐标构造方法
	 *
	 * @param longitude 经度整数值（自动转换为BigDecimal）
	 * @param latitude  纬度整数值（自动转换为BigDecimal）
	 * @see #Coordinate(BigDecimal, BigDecimal)
	 * @since 1.0.0
	 */
	public Coordinate(long longitude, long latitude) {
		this(BigDecimal.valueOf(longitude), BigDecimal.valueOf(latitude));
	}

	/**
	 * 整数坐标构造方法
	 *
	 * @param longitude 经度整数值（自动转换为BigDecimal）
	 * @param latitude  纬度整数值（自动转换为BigDecimal）
	 * @see #Coordinate(BigDecimal, BigDecimal)
	 * @since 1.0.0
	 */
	public Coordinate(int longitude, int latitude) {
		this(BigDecimal.valueOf(longitude), BigDecimal.valueOf(latitude));
	}

	/**
	 * 度分秒格式构造方法
	 *
	 * @param longitude 经度字符串（格式示例：116°23'29.34"E）
	 * @param latitude  纬度字符串（格式示例：39°54'15.12"N）
	 * @throws NumberFormatException 当字符串格式不符合度分秒规范时抛出
	 * @see CoordinateUtils#fromDMS(String)
	 * @since 1.0.0
	 */
	public Coordinate(String longitude, String latitude) {
		this(CoordinateUtils.fromDMS(longitude), CoordinateUtils.fromDMS(latitude));
	}

	/**
	 * 判断坐标是否在中国境外
	 *
	 * @return true表示不在中国境内范围
	 * @see GeoConstants#CHINA_MIN_LONGITUDE
	 * @see GeoConstants#CHINA_MAX_LONGITUDE
	 * @see GeoConstants#CHINA_MIN_LATITUDE
	 * @see GeoConstants#CHINA_MAX_LATITUDE
	 * @since 1.0.0
	 */
	public boolean isOutOfChina() {
		return !(longitude.compareTo(GeoConstants.CHINA_MIN_LONGITUDE) > 0 &&
			longitude.compareTo(GeoConstants.CHINA_MAX_LONGITUDE) < 0 &&
			latitude.compareTo(GeoConstants.CHINA_MIN_LATITUDE) > 0 &&
			latitude.compareTo(GeoConstants.CHINA_MAX_LATITUDE) < 0);
	}

	/**
	 * 转换为度分秒格式字符串
	 *
	 * @return 格式示例：116°23'29.34"E,39°54'15.12"N
	 * @see CoordinateUtils#toDMS(BigDecimal)
	 * @since 1.0.0
	 */
	@Override
	public String toString() {
		return CoordinateUtils.toDMS(longitude) + "," + CoordinateUtils.toDMS(latitude);
	}
}
