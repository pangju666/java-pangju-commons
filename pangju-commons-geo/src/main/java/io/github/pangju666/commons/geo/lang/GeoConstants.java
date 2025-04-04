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
	 * 度符号(°)
	 * <p>用于表示经纬度的度单位</p>
	 *
	 * @since 1.0.0
	 */
	public static final char RADIUS_CHAR = '°';
	/**
	 * 分符号(')
	 * <p>用于表示经纬度的分单位</p>
	 *
	 * @since 1.0.0
	 */
	public static final char MINUTE_CHAR = '\'';
	/**
	 * 秒符号(")
	 * <p>用于表示经纬度的秒单位</p>
	 *
	 * @since 1.0.0
	 */
	public static final char SECONDS_CHAR = '\"';

	/**
	 * 北方标识符(N)
	 * <p>用于表示北纬或向北方向</p>
	 *
	 * @since 1.0.0
	 */
	public static final char North_DIRECTION = 'N';
	/**
	 * 南方标识符(S)
	 * <p>用于表示南纬或向南方向</p>
	 *
	 * @since 1.0.0
	 */
	public static final char SOUTH_CHAR = 'S';
	/**
	 * 东方标识符(E)
	 * <p>用于表示东经或向东方向</p>
	 *
	 * @since 1.0.0
	 */
	public static final char EAST_CHAR = 'E';
	/**
	 * 西方标识符(W)
	 * <p>用于表示西经或向西方向</p>
	 *
	 * @since 1.0.0
	 */
	public static final char WEST_CHAR = 'W';

	/**
	 * 全球最小纬度值(-90°)
	 * <p>表示地球南极点的纬度</p>
	 *
	 * @since 1.0.0
	 */
	public static final double MIN_LATITUDE = -90;
	/**
	 * 全球最大纬度值(90°)
	 * <p>表示地球北极点的纬度</p>
	 *
	 * @since 1.0.0
	 */
	public static final double MAX_LATITUDE = 90;
	/**
	 * 全球最小经度值(-180°)
	 * <p>表示国际日期变更线以西的经度</p>
	 *
	 * @since 1.0.0
	 */
	public static final double MIN_LONGITUDE = -180;
	/**
	 * 全球最大经度值(180°)
	 * <p>表示国际日期变更线以东的经度</p>
	 *
	 * @since 1.0.0
	 */
	public static final double MAX_LONGITUDE = 180;
	/**
	 * 中国实际最小纬度值(0.8293°)
	 * <p>表示中国最南端曾母暗沙的纬度</p>
	 *
	 * @since 1.0.0
	 */
	public static final BigDecimal CHINA_MIN_LATITUDE = BigDecimal.valueOf(0.8293);
	/**
	 * 中国实际最大纬度值(55.8271°)
	 * <p>表示中国最北端漠河的纬度</p>
	 *
	 * @since 1.0.0
	 */
	public static final BigDecimal CHINA_MAX_LATITUDE = BigDecimal.valueOf(55.8271);
	/**
	 * 中国实际最小经度值(72.004°)
	 * <p>表示中国最西端帕米尔高原的经度</p>
	 *
	 * @since 1.0.0
	 */
	public static final BigDecimal CHINA_MIN_LONGITUDE = BigDecimal.valueOf(72.004);
	/**
	 * 中国实际最大经度值(137.8347°)
	 * <p>表示中国最东端黑龙江与乌苏里江交汇处的经度</p>
	 *
	 * @since 1.0.0
	 */
	public static final BigDecimal CHINA_MAX_LONGITUDE = BigDecimal.valueOf(137.8347);

	protected GeoConstants() {
	}
}
