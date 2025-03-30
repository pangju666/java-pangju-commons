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

package io.github.pangju666.commons.geo.lang;

import java.math.BigDecimal;

/**
 * GEO地理信息相关常量
 *
 * @author pangju666
 * @since 1.0.0
 */
public class GeoConstants {
	/**
	 * 度
	 *
	 * @since 1.0.0
	 */
	public static final char RADIUS_CHAR = '°';
	/**
	 * 分
	 *
	 * @since 1.0.0
	 */
	public static final char MINUTE_CHAR = '\'';
	/**
	 * 秒
	 *
	 * @since 1.0.0
	 */
	public static final char SECONDS_CHAR = '\"';
	/**
	 * 北
	 *
	 * @since 1.0.0
	 */
	public static final char North_DIRECTION = 'N';
	/**
	 * 南
	 *
	 * @since 1.0.0
	 */
	public static final char SOUTH_CHAR = 'S';
	/**
	 * 东
	 *
	 * @since 1.0.0
	 */
	public static final char EAST_CHAR = 'E';
	/**
	 * 西
	 *
	 * @since 1.0.0
	 */
	public static final char WEST_CHAR = 'W';

	/**
	 * 中国最小纬度
	 *
	 * @since 1.0.0
	 */
	public static final double MIN_LATITUDE = -90;
	/**
	 * 最小纬度
	 *
	 * @since 1.0.0
	 */
	public static final double MAX_LATITUDE = 90;
	/**
	 * 最大纬度
	 *
	 * @since 1.0.0
	 */
	public static final double MIN_LONGITUDE = -180;
	/**
	 * 最大经度
	 *
	 * @since 1.0.0
	 */
	public static final double MAX_LONGITUDE = 180;

	/**
	 * 中国最小纬度
	 *
	 * @since 1.0.0
	 */
	public static final BigDecimal CHINA_MIN_LATITUDE = BigDecimal.valueOf(0.8293);
	/**
	 * 中国最大纬度
	 *
	 * @since 1.0.0
	 */
	public static final BigDecimal CHINA_MAX_LATITUDE = BigDecimal.valueOf(55.8271);
	/**
	 * 中国最小经度
	 *
	 * @since 1.0.0
	 */
	public static final BigDecimal CHINA_MIN_LONGITUDE = BigDecimal.valueOf(72.004);
	/**
	 * 中国最大经度
	 *
	 * @since 1.0.0
	 */
	public static final BigDecimal CHINA_MAX_LONGITUDE = BigDecimal.valueOf(137.8347);

	protected GeoConstants() {
	}
}
