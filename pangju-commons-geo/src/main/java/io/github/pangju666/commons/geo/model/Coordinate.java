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
 * <p>
 * 表示一个具有高精度的地理坐标点，封装了坐标验证、格式转换和位置判断功能。
 * 本类使用BigDecimal保证计算精度，适用于地理信息系统(GIS)、地图服务等场景。
 * </p>
 *
 * <h3>核心特性</h3>
 * <table border="1">
 *   <tr><th>特性</th><th>说明</th><th>实现方式</th></tr>
 *   <tr><td>不可变性</td><td>线程安全</td><td>record类型自动实现</td></tr>
 *   <tr><td>精度保障</td><td>避免浮点误差</td><td>使用BigDecimal存储</td></tr>
 *   <tr><td>自动验证</td><td>构造时校验</td><td>Apache Commons Validate</td></tr>
 *   <tr><td>格式转换</td><td>支持多种格式</td><td>CoordinateUtils工具类</td></tr>
 * </table>
 *
 * <h3>典型应用</h3>
 * <ul>
 *   <li>地图坐标点存储</li>
 *   <li>GPS数据处理</li>
 *   <li>坐标系统转换</li>
 *   <li>地理围栏判断</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 * @see GeoConstants
 * @see CoordinateUtils
 */
public record Coordinate(BigDecimal longitude, BigDecimal latitude) {
	/**
	 * 主构造方法
	 * <p>
	 * 创建坐标对象并进行严格验证：
	 * <ul>
	 *   <li>参数非空检查</li>
	 *   <li>经度范围：[-180, 180]</li>
	 *   <li>纬度范围：[-90, 90]</li>
	 * </ul>
	 * </p>
	 *
	 * @param longitude 经度值，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>在有效范围内</li>
	 *                  </ul>
	 * @param latitude 纬度值，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>在有效范围内</li>
	 *                 </ul>
	 * @throws IllegalArgumentException 当参数不符合要求时抛出
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
	 * <p>
	 * 将double类型坐标转换为BigDecimal后创建对象，
	 * 适用于从GPS设备等获取的原始坐标数据。
	 * </p>
	 *
	 * @param longitude 经度（十进制度数），示例：116.404
	 * @param latitude 纬度（十进制度数），示例：39.915
	 * @see #Coordinate(BigDecimal, BigDecimal)
	 * @since 1.0.0
	 */
	public Coordinate(double longitude, double latitude) {
		this(BigDecimal.valueOf(longitude), BigDecimal.valueOf(latitude));
	}

	/**
	 * 度分秒格式构造方法
	 * <p>
	 * 解析度分秒(DMS)格式字符串创建坐标对象，
	 * 支持标准格式如：116°23'29.34"E。
	 * </p>
	 *
	 * @param longitude 经度字符串，示例：116°23'29.34"E
	 * @param latitude 纬度字符串，示例：39°54'15.12"N
	 * @throws NumberFormatException 当格式不符合规范时抛出
	 * @see CoordinateUtils#fromDMS(String)
	 * @since 1.0.0
	 */
	public Coordinate(String longitude, String latitude) {
		this(CoordinateUtils.fromDMS(longitude), CoordinateUtils.fromDMS(latitude));
	}

	/**
	 * 中国境内判断
	 * <p>
	 * 根据中国地理边界判断坐标是否在境外，
	 * 边界值参考GeoConstants中的定义。
	 * </p>
	 *
	 * @return 判断结果：
	 *         <ul>
	 *           <li>true - 境外或边界上</li>
	 *           <li>false - 境内</li>
	 *         </ul>
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
	 * 度分秒格式输出
	 * <p>
	 * 将坐标转换为标准度分秒表示，
	 * 格式示例：116°23'29.34"E,39°54'15.12"N
	 * </p>
	 *
	 * @return 格式化后的坐标字符串
	 * @see CoordinateUtils#toLongitudeDms(BigDecimal)
	 * @see CoordinateUtils#toLatitudeDMS(BigDecimal)
	 * @since 1.0.0
	 */
	@Override
	public String toString() {
		return CoordinateUtils.toLongitudeDms(longitude) + "," + CoordinateUtils.toLatitudeDMS(latitude);
	}
}
