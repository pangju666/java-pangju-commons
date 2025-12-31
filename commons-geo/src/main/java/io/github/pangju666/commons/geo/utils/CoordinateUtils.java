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
import org.apache.commons.lang3.Validate;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalCoordinates;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 地理坐标与几何运算工具类
 * <p>
 * 提供基于 WGS84 椭球的大地线距离、周长、面积计算，以及点在多边形内判定，
 * 同时支持常用坐标系转换（GCJ-02 与 WGS84）与度分秒格式处理。内部使用 BigDecimal
 * 与 BigDecimalMath 保障数值稳定性，适用于 GIS 系统与地图服务等场景。
 * </p>
 *
 * <h3>核心功能</h3>
 * <table border="1">
 *   <tr><th>功能类别</th><th>方法</th><th>说明/精度</th></tr>
 *   <tr><td>坐标系转换</td><td>WGS84ToGCJ02/GCJ02ToWGS84</td><td>中国境内 ±50–500 米</td></tr>
 *   <tr><td>格式转换</td><td>toLatitudeDMS/toLongitudeDms/fromDMS</td><td>DMS 精度 0.01″</td></tr>
 *   <tr><td>距离/周长</td><td>calculateDistance/calculatePerimeter</td><td>WGS84 椭球大地线</td></tr>
 *   <tr><td>面积计算</td><td>calculateArea</td><td>球面近似，误差通常 <0.1%</td></tr>
 *   <tr><td>点内判定</td><td>isPointInPolygon</td><td>射线交叉法，支持 ±180° 经线跨越</td></tr>
 * </table>
 *
 * <h3>算法与数值策略</h3>
 * <ul>
 *   <li>距离/周长：使用 {@link GeodeticCalculator} 基于 WGS84 椭球获取椭球距离</li>
 *   <li>面积：三角剖分 + L’Huilier 公式，采用平均曲率半径进行球面近似</li>
 *   <li>点内判定：经度相对参考经度标准化（避免国际日期变更线断裂）+ 射线交叉法，边界点视为“内部”</li>
 *   <li>高精度：大量中间计算使用 {@link BigDecimal} 与 BigDecimalMath 控制误差传播</li>
 * </ul>
 *
 * <h3>输入规范</h3>
 * <ul>
 *   <li>坐标系：除 GCJ/WGS 转换外，其余几何运算均假定输入为 WGS84，经纬度单位为“度”</li>
 *   <li>多边形：至少 3 个非空顶点，建议为简单多边形（无自相交）</li>
 *   <li>边界处理：点在边或顶点上视为内部；零长度边按顶点重合判定</li>
 * </ul>
 *
 * <h3>复杂度与适用性</h3>
 * <ul>
 *   <li>点内判定：时间复杂度 O(n)，空间复杂度 O(n)</li>
 *   <li>适用于一般尺度下的地图/围栏计算；极点附近或极端退化形状可能需更严格的数值策略</li>
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
	/**
	 * 椭球大地线计算器
	 * <p>
	 * 基于 WGS84 椭球的测地线计算工具，用于获取两点间的椭球距离等几何量。
	 * 作为单例常量复用以避免不必要的对象创建；该类型不包含共享可变状态，复用是安全的。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	protected static final GeodeticCalculator GEODETIC_CALCULATOR = new GeodeticCalculator();

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
	 * 计算两坐标间的大地线距离（单位：米）
	 * <p>
	 * 使用 WGS84 椭球模型，通过 {@link GeodeticCalculator} 计算起点与终点的椭球距离。
	 * 输入坐标为经纬度（度），返回值为测地线距离（Ellipsoidal Distance）。
	 * </p>
	 *
	 * @param start 起点坐标（WGS84，经纬度），不可为 {@code null}
	 * @param end   终点坐标（WGS84，经纬度），不可为 {@code null}
	 * @return 距离（米）
	 * @throws IllegalArgumentException 如果 {@code start} 或 {@code end} 为 {@code null}
	 * @since 1.0.0
	 */
	public static double calculateDistance(final Coordinate start, final Coordinate end) {
		Validate.notNull(start, "start 不可为null");
		Validate.notNull(end, "end 不可为null");

		GlobalCoordinates startGlobalCoordinates = new GlobalCoordinates(
			start.getLatitude().doubleValue(), start.getLongitude().doubleValue());
		GlobalCoordinates endGlobalCoordinates = new GlobalCoordinates(
			end.getLatitude().doubleValue(), end.getLongitude().doubleValue());
		return GEODETIC_CALCULATOR.calculateGeodeticCurve(Ellipsoid.WGS84, startGlobalCoordinates,
			endGlobalCoordinates).getEllipsoidalDistance(); // 单位：米
	}

	/**
	 * 计算 WGS84 多边形的周长（单位：米）
	 * <p>
	 * 方法说明：
	 * <ul>
	 *   <li>对连续顶点间的大地线距离求和（基于 WGS84 椭球）</li>
	 *   <li>若首尾点不相同，自动闭合以保证周长计算完整</li>
	 *   <li>顶点顺序对结果无影响（顺/逆时针均可）</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 输入要求与限制：
	 * <ul>
	 *   <li>坐标系需为 WGS84，经纬度单位为度</li>
	 *   <li>至少 3 个非空顶点，且建议为简单多边形（无自相交）</li>
	 * </ul>
	 * </p>
	 *
	 * @param coordinates 多边形顶点列表，至少包含 3 个非空坐标点
	 * @return 周长（米），以 {@link MathContext#DECIMAL32} 精度返回
	 * @throws IllegalArgumentException 如果输入为空、有效点数不足（<3）或包含 {@code null} 坐标
	 * @since 1.0.0
	 */
	public static BigDecimal calculatePerimeter(final Collection<Coordinate> coordinates) {
		if (coordinates == null || coordinates.isEmpty()) {
			throw new IllegalArgumentException("coordinates 不可为空");
		}

		List<Coordinate> validCoordinates = coordinates.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		if (validCoordinates.size() < 3) {
			throw new IllegalArgumentException("至少需要 3 个有效坐标才能计算周长，但只有：" + validCoordinates.size() + "个");
		}

		// 自动闭合
		List<Coordinate> closed = new ArrayList<>(validCoordinates);
		Coordinate first = validCoordinates.get(0);
		Coordinate last = validCoordinates.get(validCoordinates.size() - 1);
		if (first.getLatitude().compareTo(last.getLatitude()) != 0 ||
			first.getLongitude().compareTo(last.getLongitude()) != 0) {
			closed.add(first);
		}

		BigDecimal totalDistance = BigDecimal.ZERO;
		for (int i = 0; i < closed.size() - 1; i++) {
			Coordinate p1 = closed.get(i);
			Coordinate p2 = closed.get(i + 1);
			double segment = calculateDistance(p1, p2);
			totalDistance = totalDistance.add(BigDecimal.valueOf(segment), MathContext.DECIMAL32);
		}

		return totalDistance;
	}

	/**
	 * 计算 WGS84 多边形的面积（单位：平方米）
	 * <p>
	 * 算法概述：
	 * <ul>
	 *   <li>以第一个顶点为公共顶点，将多边形进行三角剖分为若干三角形</li>
	 *   <li>对每个三角形，使用 {@link GeodeticCalculator} 获取三边的大地线距离（基于 WGS84 椭球）</li>
	 *   <li>采用 L’Huilier 公式计算球面角盈并以平均曲率半径近似求得球面面积</li>
	 *   <li>累加各三角形面积并取绝对值</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 注意与限制：
	 * <ul>
	 *   <li>输入坐标必须为 WGS84 坐标系（经纬度）</li>
	 *   <li>多边形应为简单多边形（无自相交）；复杂边界可能产生不稳定结果</li>
	 *   <li>顶点顺序决定面积符号（逆时针为正，顺时针为负），最终返回绝对值</li>
	 *   <li>若首尾点不相同，会自动闭合</li>
	 *   <li>使用球面近似计算面积（以 WGS84 半长轴与半短轴的平均值作为半径）；在一般尺度下误差通常小于 0.1%</li>
	 * </ul>
	 * </p>
	 *
	 * @param coordinates 多边形顶点列表，至少包含 3 个非空坐标点
	 * @return 面积（平方米），以 {@link MathContext#DECIMAL32} 精度返回，结果为非负数
	 * @throws IllegalArgumentException 如果输入为空、有效点数不足（<3）或包含 {@code null} 坐标
	 * @since 1.0.0
	 */
	public static BigDecimal calculateArea(final Collection<Coordinate> coordinates) {
		if (Objects.isNull(coordinates) || coordinates.isEmpty()) {
			throw new IllegalArgumentException("coordinates 不可为空");
		}

		List<Coordinate> validCoordinates = coordinates.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		if (validCoordinates.size() < 3) {
			throw new IllegalArgumentException("至少需要 3 个有效坐标才能计算面积，但只有：" + validCoordinates.size() + "个");
		}

		// 自动闭合：如果首尾不一致，复制第一个点到末尾
		List<Coordinate> closedCoordinates = new ArrayList<>(validCoordinates);
		Coordinate first = validCoordinates.get(0);
		Coordinate last = validCoordinates.get(validCoordinates.size() - 1);
		if (first.getLatitude().compareTo(last.getLatitude()) != 0 ||
			first.getLongitude().compareTo(last.getLongitude()) != 0) {
			closedCoordinates.add(first);
		}

		BigDecimal totalArea = BigDecimal.ZERO;
		Coordinate originCoordinate = closedCoordinates.get(0);
		GlobalCoordinates origin = new GlobalCoordinates(originCoordinate.getLatitude().doubleValue(),
			originCoordinate.getLongitude().doubleValue());

		// 以第一个点为公共顶点，三角剖分：[0, i, i+1]
		int n = closedCoordinates.size();
		for (int i = 1; i < n - 1; i++) {
			Coordinate closedCoordinate1 = closedCoordinates.get(i);
			GlobalCoordinates p1 = new GlobalCoordinates(closedCoordinate1.getLatitude().doubleValue(),
				closedCoordinate1.getLongitude().doubleValue());

			Coordinate closedCoordinate2 = closedCoordinates.get(i + 1);
			GlobalCoordinates p2 = new GlobalCoordinates(closedCoordinate2.getLatitude().doubleValue(),
				closedCoordinate2.getLongitude().doubleValue());

			// 计算三角形 (origin, p1, p2) 的球面面积
			BigDecimal triangleArea = calculateTriangleArea(origin, p1, p2);
			totalArea = totalArea.add(triangleArea, MathContext.DECIMAL32);
		}

		return totalArea.abs(MathContext.DECIMAL32); // 取绝对值，忽略方向
	}

	/**
	 * 判断给定点是否位于简单多边形内部或边界上（射线交叉法）
	 * <p>
	 * 算法说明：
	 * <ul>
	 *   <li>通过 {@link #adjustLongitude(double, double)} 将多边形与测试点的经度相对参考经度标准化，避免跨越国际日期变更线导致的射线法失效</li>
	 *   <li>在标准化后的坐标上，使用水平向右的射线与边相交次数奇偶判断点是否在多边形内</li>
	 *   <li>边界判定通过 {@link #isPointOnSegment(double, double, double, double, double, double)} 完成，边界上的点视为“内部”</li>
	 *   <li>为增强数值稳定性，跳过近似水平的边（|Δy| < 1e-12）</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 支持与限制：
	 * <ul>
	 *   <li>支持凸/凹简单多边形</li>
	 *   <li>自动闭合（若首尾不一致）</li>
	 *   <li>支持经度跨越 ±180°（通过经度标准化处理）</li>
	 *   <li>不支持自相交多边形（如“8”字形）；极点附近可能存在精度问题</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 复杂度：
	 * <ul>
	 *   <li>时间复杂度 O(n)</li>
	 *   <li>空间复杂度 O(n)</li>
	 * </ul>
	 * </p>
	 *
	 * @param point   待检测点（不可为 {@code null}）
	 * @param polygon 多边形顶点（至少 3 个非空点）
	 * @return 如果点在多边形内部或边上返回 {@code true}，否则返回 {@code false}
	 * @throws IllegalArgumentException 如果输入非法
	 * @since 1.0.0
	 */
	public static boolean isPointInPolygon(final Coordinate point, final Collection<Coordinate> polygon) {
		if (point == null) {
			throw new IllegalArgumentException("point 不可为 null");
		}
		if (polygon == null || polygon.isEmpty()) {
			throw new IllegalArgumentException("polygon 不可为空");
		}

		List<Coordinate> validCoordinates = polygon.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		if (validCoordinates.size() < 3) {
			throw new IllegalArgumentException("多边形至少需要 3 个有效顶点，但只有：" + validCoordinates.size() + "个");
		}

		int n = validCoordinates.size();

		// 参考经度
		double refLongitude = validCoordinates.get(0).getLongitude().doubleValue();

		// 调整多边形所有点
		List<double[]> adjustedPoly = new ArrayList<>();
		for (Coordinate c : validCoordinates) {
			double lat = c.getLatitude().doubleValue();
			double lon = c.getLongitude().doubleValue();
			double adjustedLon = adjustLongitude(lon, refLongitude);
			adjustedPoly.add(new double[]{lat, adjustedLon});
		}

		// 调整测试点
		double testLat = point.getLatitude().doubleValue();
		double testLon = point.getLongitude().doubleValue();
		double adjustedTestLon = adjustLongitude(testLon, refLongitude);

		// === 在调整后的坐标上运行射线法 ===
		boolean inside = false;
		for (int i = 0, j = n - 1; i < n; j = i++) {
			double xi = adjustedPoly.get(i)[1];
			double yi = adjustedPoly.get(i)[0];
			double xj = adjustedPoly.get(j)[1];
			double yj = adjustedPoly.get(j)[0];

			// 检查是否在边上
			if (isPointOnSegment(adjustedTestLon, testLat, xi, yi, xj, yj)) {
				return true;
			}

			// 防御性：跳过水平边（避免除零，增强鲁棒性）
			if (Math.abs(yj - yi) < 1e-12) {
				continue;
			}

			// 射线交叉判断（向右发射水平射线）
			if (((yi > testLat) != (yj > testLat)) &&
				(adjustedTestLon < (xj - xi) * (testLat - yi) / (yj - yi) + xi)) {
				inside = !inside;
			}
		}

		return inside;
	}

	/**
	 * 经度标准化（相对参考经度）
	 * <p>
	 * 将给定经度映射到以参考经度为中心的区间 [refLon - 180, refLon + 180)，
	 * 以避免跨越国际日期变更线时的坐标断裂问题，从而保证射线交叉法的正确性。
	 * </p>
	 *
	 * @param lon    原始经度（度）
	 * @param refLon 参考经度（度）
	 * @return 标准化后的经度（度）
	 * @since 1.0.0
	 */
	protected static double adjustLongitude(double lon, double refLon) {
		double delta = lon - refLon;
		if (delta > 180.0) {
			delta -= 360.0;
		} else if (delta <= -180.0) {
			delta += 360.0;
		}
		return refLon + delta;
	}

	/**
	 * 判断点是否位于线段上（包含端点）
	 * <p>
	 * 数值策略：
	 * <ul>
	 *   <li>使用叉积近似 0（|cross| ≤ 1e-9）判断共线</li>
	 *   <li>使用点乘与线段长度平方判断是否位于线段包围盒内</li>
	 * </ul>
	 * </p>
	 *
	 * @param px 测试点经度（标准化后的经度）
	 * @param py 测试点纬度
	 * @param x1 线段起点经度
	 * @param y1 线段起点纬度
	 * @param x2 线段终点经度
	 * @param y2 线段终点纬度
	 * @return 若点位于线段上（含端点）返回 {@code true}
	 * @since 1.0.0
	 */
	protected static boolean isPointOnSegment(double px, double py, double x1, double y1, double x2, double y2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double squaredLen = dx * dx + dy * dy;
		if (squaredLen == 0) {
			return Math.abs(px - x1) <= 1e-12 && Math.abs(py - y1) <= 1e-12;
		}
		double cross = (py - y1) * (x2 - x1) - (px - x1) * (y2 - y1);
		if (Math.abs(cross) > 1e-9) {
			return false;
		}
		double dot = (px - x1) * (x2 - x1) + (py - y1) * (y2 - y1);
		if (dot < 0) {
			return false;
		}
		return dot <= squaredLen;
	}

	/**
	 * 计算 WGS84 球面近似下三角形面积（单位：平方米）
	 * <p>
	 * 方法说明：
	 * <ul>
	 *   <li>使用 {@link GeodeticCalculator} 计算三边的大地线距离（基于 WGS84 椭球）</li>
	 *   <li>将三边距离转换为球面弧度（以 WGS84 半长轴与半短轴的平均值作为半径）</li>
	 *   <li>利用 L’Huilier 公式计算球面角盈（spherical excess）</li>
	 *   <li>面积 = 角盈 × R²</li>
	 * </ul>
	 * 数值稳定性：若退化为近乎共线的三角形或角盈计算出现非正数，将返回 0。
	 * </p>
	 *
	 * @param a 顶点 A 的全球坐标（纬度、经度，单位：度）
	 * @param b 顶点 B 的全球坐标（纬度、经度，单位：度）
	 * @param c 顶点 C 的全球坐标（纬度、经度，单位：度）
	 * @return 三角形面积（平方米），以 {@link MathContext#DECIMAL32} 精度返回
	 * @since 1.0.0
	 */
	protected static BigDecimal calculateTriangleArea(GlobalCoordinates a, GlobalCoordinates b, GlobalCoordinates c) {
		// 计算三边的大地线距离（米）
		// 对边 A
		double distanceA = GEODETIC_CALCULATOR.calculateGeodeticCurve(Ellipsoid.WGS84, b, c).getEllipsoidalDistance();
		// 对边 B
		double distanceB = GEODETIC_CALCULATOR.calculateGeodeticCurve(Ellipsoid.WGS84, a, c).getEllipsoidalDistance();
		// 对边 C
		double distanceC = GEODETIC_CALCULATOR.calculateGeodeticCurve(Ellipsoid.WGS84, a, b).getEllipsoidalDistance();

		// 转换为球面弧度（使用平均曲率半径近似）
		// 注：严格椭球面积需复杂积分，此处用球面近似已足够（误差 < 0.1%）
		double R = (Ellipsoid.WGS84.getSemiMajorAxis() + Ellipsoid.WGS84.getSemiMinorAxis()) / 2.0;
		double aRad = distanceA / R;
		double bRad = distanceB / R;
		double cRad = distanceC / R;

		// 半周长
		double s = (aRad + bRad + cRad) / 2.0;

		// 防止数值不稳定（如退化三角形）
		if (s <= aRad || s <= bRad || s <= cRad || s <= 0) {
			return BigDecimal.ZERO;
		}

		// L’Huilier 公式计算球面角盈 E
		double tanESq = Math.tan(s / 2.0)
			* Math.tan((s - aRad) / 2.0)
			* Math.tan((s - bRad) / 2.0)
			* Math.tan((s - cRad) / 2.0);

		if (tanESq <= 0) {
			return BigDecimal.ZERO;
		}

		double E = 4.0 * Math.atan(Math.sqrt(tanESq)); // spherical excess in radians

		// 面积 = E * R^2
		double area = E * R * R;

		return BigDecimal.valueOf(area).round(MathContext.DECIMAL32);
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
			(latitude ? GeoConstants.NORTH_DIRECTION : GeoConstants.EAST_CHAR);

		// 格式化输出
		return String.format(DMS_FORMAT, degrees.toPlainString(), minutes.toPlainString(), seconds.doubleValue(), direction);
	}
}
