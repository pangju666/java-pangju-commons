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
 * 地理坐标系类型枚举
 * <p>
 * 定义并管理常用的地理坐标系标准，提供坐标系间的转换能力。
 * 本枚举封装了不同坐标系间的转换逻辑，确保坐标数据在不同标准间的正确转换。
 * </p>
 *
 * <h3>坐标系技术规范</h3>
 * <table border="1">
 *   <tr><th>坐标系</th><th>标准</th><th>使用范围</th><th>精度</th></tr>
 *   <tr><td>GCJ-02</td><td>中国国家测绘标准</td><td>中国大陆地图服务</td><td>±50米</td></tr>
 *   <tr><td>WGS-84</td><td>国际GPS标准</td><td>全球定位系统</td><td>±5米</td></tr>
 * </table>
 *
 * <h3>典型应用场景</h3>
 * <ul>
 *   <li>地图服务坐标系统一处理</li>
 *   <li>GPS设备数据转换</li>
 *   <li>多地图平台数据兼容</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 * @see Coordinate
 * @see CoordinateUtils
 */
public enum CoordinateType {
	/**
	 * 国测局坐标系（火星坐标系）
	 * <p>
	 * 中国官方地图坐标系统，具有以下特点：
	 * <ul>
	 *   <li>基于WGS-84进行非线性加密</li>
	 *   <li>中国大陆法律要求的公开地图标准</li>
	 *   <li>高德、腾讯、百度等地图服务采用</li>
	 * </ul>
	 *
	 * <h3>注意事项</h3>
	 * <ul>
	 *   <li>与WGS-84存在50-500米偏移</li>
	 *   <li>境外地图服务不可直接使用</li>
	 * </ul>
	 *
	 * @since 1.0.0
	 */
	GCJ_02,
	/**
	 * 世界大地坐标系
	 * <p>
	 * 国际通用GPS坐标系统，具有以下特点：
	 * <ul>
	 *   <li>GPS设备原始坐标体系</li>
	 *   <li>Google Earth等国际地图服务采用</li>
	 *   <li>中国大陆境内需转换为GCJ-02使用</li>
	 * </ul>
	 *
	 * <h3>技术参数</h3>
	 * <ul>
	 *   <li>椭球体长半轴：6378137米</li>
	 *   <li>扁率：1/298.257223563</li>
	 * </ul>
	 *
	 * @since 1.0.0
	 */
	WGS_84;

	/**
	 * 将当前坐标系坐标转换为GCJ-02坐标
	 * <p>
	 * 执行坐标转换算法，处理不同情况：
	 * <ul>
	 *   <li>当前为GCJ-02：直接返回原坐标</li>
	 *   <li>当前为WGS-84：应用火星坐标加密算法</li>
	 * </ul>
	 *
	 * @param coordinate 待转换坐标对象，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>坐标类型与当前枚举值匹配</li>
	 *                   </ul>
	 * @return 转换后的GCJ-02坐标系坐标，不会返回null
	 * @throws IllegalArgumentException 当参数为null或坐标类型不匹配时抛出
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
	 * <p>
	 * 执行坐标转换算法，处理不同情况：
	 * <ul>
	 *   <li>当前为WGS-84：直接返回原坐标</li>
	 *   <li>当前为GCJ-02：应用火星坐标解密算法</li>
	 * </ul>
	 *
	 * @param coordinate 待转换坐标对象，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>坐标类型与当前枚举值匹配</li>
	 *                   </ul>
	 * @return 转换后的WGS-84坐标系坐标，不会返回null
	 * @throws IllegalArgumentException 当参数为null或坐标类型不匹配时抛出
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
