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

package io.github.pangju666.commons.geo.enums;

import io.github.pangju666.commons.geo.model.Coordinate;
import io.github.pangju666.commons.geo.utils.CoordinateUtils;

/**
 * 坐标系类型枚举
 * <p>定义常用的地理坐标系标准，提供坐标系间的转换方法</p>
 *
 * <h3>坐标系说明：</h3>
 * <ul>
 *     <li><strong>GCJ-02</strong> - 中国国家测绘局制定的坐标体系，用于高德、腾讯等国内地图服务</li>
 *     <li><strong>WGS-84</strong> - 国际通用的GPS坐标体系，Google Earth等国际地图服务采用</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public enum CoordinateType {
	/**
	 * 国测局坐标系（火星坐标系）
	 * <p>中国地图偏移标准，适用于国内地图服务</p>
	 *
	 * @since 1.0.0
	 */
	GCJ_02,
	/**
	 * 世界大地坐标系
	 * <p>GPS原始坐标体系，国际通用标准</p>
	 *
	 * @since 1.0.0
	 */
	WGS_84;

	/**
	 * 将当前坐标系坐标转换为GCJ-02坐标
	 *
	 * @param coordinate 原始坐标（必须为当前坐标系类型）
	 * @return 转换后的GCJ-02坐标系坐标
	 * @throws IllegalArgumentException 当坐标参数为null时抛出
	 * @see CoordinateUtils#WGS84ToGCJ02(Coordinate)
	 * @since 1.0.0
	 */
	public Coordinate toGCJ02(final Coordinate coordinate) {
		return switch (this) {
			case GCJ_02 -> coordinate;
			case WGS_84 -> CoordinateUtils.WGS84ToGCJ02(coordinate);
		};
	}

	/**
	 * 将当前坐标系坐标转换为WGS-84坐标
	 *
	 * @param coordinate 原始坐标（必须为当前坐标系类型）
	 * @return 转换后的WGS-84坐标系坐标
	 * @throws IllegalArgumentException 当坐标参数为null时抛出
	 * @see CoordinateUtils#GCJ02ToWGS84(Coordinate)
	 * @since 1.0.0
	 */
	public Coordinate toWGS84(final Coordinate coordinate) {
		return switch (this) {
			case GCJ_02 -> CoordinateUtils.GCJ02ToWGS84(coordinate);
			case WGS_84 -> coordinate;
		};
	}
}
