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

package io.github.pangju666.commons.lang.utils;

import io.github.pangju666.commons.lang.pool.Constants;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * 日期时间工具类，提供日期解析、转换、计算等功能，继承了{@link org.apache.commons.lang3.time.DateUtils}的功能
 *
 * @author pangju666
 * @see org.apache.commons.lang3.time.DateUtils
 * @since 1.0.0
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
	protected DateUtils() {
	}

	/**
	 * 中国时区
	 *
	 * @since 1.0.0
	 */
	public static final ZoneId CHINA_ZONE_ID = ZoneId.of("CTT");

	/**
	 * 将字符串解析为Date对象，使用预设格式{@link Constants#DATE_FORMAT}、{@link Constants#DATETIME_FORMAT}, "yyyy-MM-dd HH:mm"
	 *
	 * @param str 要解析的日期字符串
	 * @return 解析成功的Date对象，解析失败或空输入时返回null
	 * @since 1.0.0
	 */
	public static Date parseDate(final String str) {
		return parseDateOrDefault(str, null);
	}

	/**
	 * 将字符串解析为Date对象，使用预设格式{@link Constants#DATE_FORMAT}、{@link Constants#DATETIME_FORMAT}, "yyyy-MM-dd HH:mm"
	 *
	 * @param str         要解析的日期字符串
	 * @param defaultDate 默认返回值（当解析失败或空输入时使用）
	 * @return 解析成功的Date对象，失败返回defaultDate
	 * @since 1.0.0
	 */
	public static Date parseDateOrDefault(final String str, final Date defaultDate) {
		try {
			if (StringUtils.isBlank(str)) {
				return defaultDate;
			}
			return parseDate(str, Constants.DATE_FORMAT, Constants.DATETIME_FORMAT, "yyyy-MM-dd HH:mm");
		} catch (ParseException e) {
			return defaultDate;
		}
	}

	/**
	 * 将字符串解析为Date对象，使用自定义格式
	 *
	 * @param str           要解析的日期字符串
	 * @param defaultDate   默认返回值（当解析失败或空输入时使用）
	 * @param parsePatterns 自定义日期格式模式数组
	 * @return 解析成功的Date对象，失败返回defaultDate
	 * @since 1.0.0
	 */
	public static Date parseDateOrDefault(final String str, final Date defaultDate, final String... parsePatterns) {
		try {
			if (StringUtils.isBlank(str)) {
				return defaultDate;
			}
			return parseDate(str, parsePatterns);
		} catch (ParseException e) {
			return defaultDate;
		}
	}

	/**
	 * 获取当前时间的Date对象
	 *
	 * @return 当前系统时间的Date实例
	 * @since 1.0.0
	 */
	public static Date nowDate() {
		return new Date();
	}

	/**
	 * 将时间戳转换为Date对象
	 *
	 * @param timestamp 时间戳
	 * @return 对应的Date对象，输入为null时返回null
	 * @since 1.0.0
	 */
	public static Date toDate(final Long timestamp) {
		return toDate(timestamp, null);
	}

	/**
	 * 将时间戳转换为Date对象，支持默认值
	 *
	 * @param timestamp    时间戳
	 * @param defaultValue 默认返回值
	 * @return 对应的Date对象，输入为null时返回defaultValue
	 * @since 1.0.0
	 */
	public static Date toDate(final Long timestamp, final Date defaultValue) {
		if (Objects.isNull(timestamp)) {
			return defaultValue;
		}
		return new Date(timestamp);
	}

	/**
	 * 将 {@link Instant} 转换为 {@link Date}。
	 * <p>
	 * 当 {@code instant} 为 {@code null} 时返回 {@code null}。
	 * 等价于调用 {@link #toDate(Instant, Date) toDate(instant, null)}。
	 * 转换基于时间戳，不涉及时区换算。
	 * </p>
	 *
	 * @param instant 待转换的时间点
	 * @return 转换得到的 {@link Date}；当 {@code instant} 为 {@code null} 时返回 {@code null}
	 * @since 1.0.0
	 */
	public static Date toDate(final Instant instant) {
		return toDate(instant, null);
	}

	/**
	 * 将 {@link Instant} 转换为 {@link Date}，支持空值默认返回。
	 * <p>
	 * 当 {@code instant} 为 {@code null} 时返回 {@code defaultValue}；
	 * 否则返回 {@link Date#from(Instant)}。转换基于时间戳，不涉及时区换算。
	 * </p>
	 *
	 * @param instant      待转换的时间点
	 * @param defaultValue 当 {@code instant} 为 {@code null} 时返回的默认值
	 * @return 转换得到的 {@link Date} 或 {@code defaultValue}
	 * @since 1.0.0
	 */
	public static Date toDate(final Instant instant, final Date defaultValue) {
		if (Objects.isNull(instant)) {
			return defaultValue;
		}
		return Date.from(instant);
	}

	/**
	 * 获取Date对象的时间戳
	 *
	 * @param date 日期对象
	 * @return 毫秒级时间戳，输入为null时返回null
	 * @since 1.0.0
	 */
	public static Long getTime(final Date date) {
		return getTime(date, null);
	}

	/**
	 * 获取Date对象的时间戳，支持默认值
	 *
	 * @param date         日期对象
	 * @param defaultValue 默认返回值
	 * @return 毫秒级时间戳，输入为null时返回defaultValue
	 * @since 1.0.0
	 */
	public static Long getTime(final Date date, final Long defaultValue) {
		if (Objects.isNull(date)) {
			return defaultValue;
		}
		return date.getTime();
	}

	/**
	 * 将 {@link Date} 转换为 {@link Instant}。
	 * <p>
	 * 当 {@code date} 为 {@code null} 时返回 {@code null}。等价于
	 * {@link #toInstant(Date, Instant) toInstant(date, null)}。
	 * 该转换基于时间戳，不涉及时区换算。
	 * </p>
	 *
	 * @param date 待转换的日期
	 * @return 转换得到的 {@link Instant}；当 {@code date} 为 {@code null} 时返回 {@code null}
	 * @since 1.0.0
	 */
	public static Instant toInstant(final Date date) {
		return toInstant(date, null);
	}

	/**
	 * 将 {@link Date} 转换为 {@link Instant}，支持空值默认返回。
	 * <p>
	 * 当 {@code date} 为 {@code null} 时返回 {@code defaultValue}；
	 * 否则返回 {@link Date#toInstant()}。该转换基于时间戳，不涉及时区换算。
	 * </p>
	 *
	 * @param date         待转换的日期
	 * @param defaultValue 当 {@code date} 为 {@code null} 时返回的默认值
	 * @return 转换得到的 {@link Instant} 或 {@code defaultValue}
	 * @since 1.0.0
	 */
	public static Instant toInstant(final Date date, final Instant defaultValue) {
		if (Objects.isNull(date)) {
			return defaultValue;
		}
		return date.toInstant();
	}

	/**
	 * 计算两个日期之间的毫秒差
	 *
	 * @param date1 第一个日期
	 * @param date2 第二个日期
	 * @return 时间差的毫秒数，任一输入为null时返回null
	 * @since 1.0.0
	 */
	public static Long betweenMillis(final Date date1, final Date date2) {
		return Optional.ofNullable(between(date1, date2)).map(Duration::toMillis).orElse(null);
	}

	/**
	 * 计算两个日期之间的秒数差
	 *
	 * @param date1 第一个日期
	 * @param date2 第二个日期
	 * @return 时间差的秒数，任一输入为null时返回null
	 * @since 1.0.0
	 */
	public static Long betweenSeconds(final Date date1, final Date date2) {
		return Optional.ofNullable(between(date1, date2)).map(Duration::toSeconds).orElse(null);
	}

	/**
	 * 计算两个日期之间的分钟差
	 *
	 * @param date1 第一个日期
	 * @param date2 第二个日期
	 * @return 时间差的分钟数，任一输入为null时返回null
	 * @since 1.0.0
	 */
	public static Long betweenMinutes(final Date date1, final Date date2) {
		return Optional.ofNullable(between(date1, date2)).map(Duration::toMinutes).orElse(null);
	}

	/**
	 * 计算两个日期之间的小时差
	 *
	 * @param date1 第一个日期
	 * @param date2 第二个日期
	 * @return 时间差的小时数，任一输入为null时返回null
	 * @since 1.0.0
	 */
	public static Long betweenHours(final Date date1, final Date date2) {
		return Optional.ofNullable(between(date1, date2)).map(Duration::toHours).orElse(null);
	}

	/**
	 * 计算两个日期之间的天数差
	 *
	 * @param date1 第一个日期
	 * @param date2 第二个日期
	 * @return 时间差的天数，任一输入为null时返回null
	 * @since 1.0.0
	 */
	public static Long betweenDays(final Date date1, final Date date2) {
		return Optional.ofNullable(between(date1, date2)).map(Duration::toDays).orElse(null);
	}

	/**
	 * 计算两个日期之间的Duration对象
	 *
	 * @param date1 第一个日期
	 * @param date2 第二个日期
	 * @return Duration对象，任一输入为null时返回null
	 * @since 1.0.0
	 */
	protected static Duration between(final Date date1, final Date date2) {
		if (ObjectUtils.anyNull(date1, date2)) {
			return null;
		}
		long amount = Math.abs(date1.getTime() - date2.getTime());
		return Duration.of(amount, ChronoUnit.MILLIS);
	}

	/**
	 * 计算两个日期之间的年份差（截断计算）
	 *
	 * @param date1 第一个日期
	 * @param date2 第二个日期
	 * @return 年份差的绝对值，任一输入为null时返回null
	 * @since 1.0.0
	 */
	public static Integer truncateBetweenYears(final Date date1, final Date date2) {
		return truncateBetween(date1, date2, Calendar.YEAR);
	}

	/**
	 * 计算两个日期之间的月份差（截断计算）
	 *
	 * @param date1 第一个日期
	 * @param date2 第二个日期
	 * @return 月份差的绝对值，任一输入为null时返回null
	 * @since 1.0.0
	 */
	public static Integer truncateBetweenMonths(final Date date1, final Date date2) {
		return truncateBetween(date1, date2, Calendar.MONTH);
	}

	/**
	 * 计算两个日期之间的天数差（截断计算）
	 *
	 * @param date1 第一个日期
	 * @param date2 第二个日期
	 * @return 天数差的绝对值，任一输入为null时返回null
	 * @since 1.0.0
	 */
	public static Integer truncateBetweenDays(final Date date1, final Date date2) {
		return truncateBetween(date1, date2, Calendar.DATE);
	}

	/**
	 * 计算两个日期之间的小时差（截断计算）
	 *
	 * @param date1 第一个日期
	 * @param date2 第二个日期
	 * @return 小时差的绝对值，任一输入为null时返回null
	 * @since 1.0.0
	 */
	public static Integer truncateBetweenHours(final Date date1, final Date date2) {
		return truncateBetween(date1, date2, Calendar.HOUR);
	}

	/**
	 * 计算两个日期之间的分钟差（截断计算）
	 *
	 * @param date1 第一个日期
	 * @param date2 第二个日期
	 * @return 分钟差的绝对值，任一输入为null时返回null
	 * @since 1.0.0
	 */
	public static Integer truncateBetweenMinutes(final Date date1, final Date date2) {
		return truncateBetween(date1, date2, Calendar.MINUTE);
	}

	/**
	 * 计算两个日期之间的秒数差（截断计算）
	 *
	 * @param date1 第一个日期
	 * @param date2 第二个日期
	 * @return 秒数差的绝对值，任一输入为null时返回null
	 * @since 1.0.0
	 */
	public static Integer truncateBetweenSeconds(final Date date1, final Date date2) {
		return truncateBetween(date1, date2, Calendar.SECOND);
	}

	/**
	 * 通用截断计算时间差方法
	 *
	 * @param date1 第一个日期
	 * @param date2 第二个日期
	 * @param field 时间字段（Calendar常量）
	 * @return 指定时间字段的差值绝对值，任一输入为null时返回null
	 * @since 1.0.0
	 */
	public static Integer truncateBetween(final Date date1, final Date date2, final int field) {
		if (ObjectUtils.anyNull(date1, date2)) {
			return null;
		}
		int amount1 = toCalendar(date1).get(field);
		int amount2 = toCalendar(date2).get(field);
		return Math.abs(amount1 - amount2);
	}

	/**
	 * 计算年龄（基于年份差值）
	 *
	 * @param birthDate 出生日期
	 * @return 当前年龄（周岁），输入为null时返回null
	 * @since 1.0.0
	 */
	public static Integer calculateAge(final Date birthDate) {
		if (Objects.isNull(birthDate)) {
			return null;
		}
		int nowYear = toCalendar(nowDate()).get(Calendar.YEAR);
		int birthYear = toCalendar(birthDate).get(Calendar.YEAR);
		return nowYear - birthYear;
	}
}