package io.github.pangju666.commons.gis.lang;

import java.math.BigDecimal;

public class Constants {
	// 度
	public static final char RADIUS_CHAR = '°';
	// 分
	public static final char MINUTE_CHAR = '′';
	// 秒
	public static final char SECONDS_CHAR = '″';
	// 中国最小纬度
	public static final BigDecimal CHINA_MIN_LATITUDE = BigDecimal.valueOf(0.8293);
	// 中国最大纬度
	public static final BigDecimal CHINA_MAX_LATITUDE = BigDecimal.valueOf(55.8271);
	// 中国最小经度
	public static final BigDecimal CHINA_MIN_LONGITUDE = BigDecimal.valueOf(72.004);
	// 中国最大经度
	public static final BigDecimal CHINA_MAX_LONGITUDE = BigDecimal.valueOf(137.8347);
	// 圆周率
	public static final BigDecimal PI = BigDecimal.valueOf(3.1415926535897932384626);
	// 长半轴
	public static final BigDecimal A = BigDecimal.valueOf(6378245.0);
	// 偏心率平方
	public static final BigDecimal EE = BigDecimal.valueOf(0.00669342162296594323);

	protected Constants() {
	}
}
