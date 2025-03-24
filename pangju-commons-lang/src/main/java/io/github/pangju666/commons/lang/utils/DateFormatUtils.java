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

import java.util.Date;
import java.util.Objects;

/**
 * 日期格式化工具类，继承了{@link org.apache.commons.lang3.time.DateFormatUtils}的功能
 *
 * @author pangju666
 * @see org.apache.commons.lang3.time.DateFormatUtils
 * @since 1.0.0
 */
public class DateFormatUtils extends org.apache.commons.lang3.time.DateFormatUtils {
	protected DateFormatUtils() {
	}

	/**
	 * 使用{@link Constants#DATETIME_FORMAT}格式化当前日期时间
	 *
	 * @return 格式化后的日期时间
	 * @since 1.0.0
	 */
	public static String formatDatetime() {
		return format(new Date(), Constants.DATETIME_FORMAT);
	}

	/**
	 * 使用{@link Constants#DATETIME_FORMAT}格式化日期时间
	 *
	 * @param date 日期时间，为null则返回空字符串
	 * @return 格式化后的日期时间
	 * @since 1.0.0
	 */
	public static String formatDatetime(final Date date) {
		if (Objects.isNull(date)) {
			return StringUtils.EMPTY;
		}
		return format(date, Constants.DATETIME_FORMAT);
	}

	/**
	 * 使用{@link Constants#DATETIME_FORMAT}格式化时间戳
	 *
	 * @param timestamp 时间戳，为null则返回空字符串
	 * @return 格式化后的时间戳
	 * @since 1.0.0
	 */
	public static String formatDatetime(final Long timestamp) {
		if (Objects.isNull(timestamp)) {
			return StringUtils.EMPTY;
		}
		return format(timestamp, Constants.DATETIME_FORMAT);
	}

	/**
	 * 使用{@link Constants#DATE_FORMAT}格式化当前日期
	 *
	 * @return 格式化后的日期
	 * @since 1.0.0
	 */
	public static String formatDate() {
		return formatDate(new Date());
	}

	/**
	 * 使用{@link Constants#DATE_FORMAT}格式化日期
	 *
	 * @param date 日期，为null则返回空字符串
	 * @return 格式化后的日期
	 * @since 1.0.0
	 */
	public static String formatDate(final Date date) {
		if (Objects.isNull(date)) {
			return StringUtils.EMPTY;
		}
		return format(date, Constants.DATE_FORMAT);
	}

	/**
	 * 使用{@link Constants#DATE_FORMAT}格式化时间戳
	 *
	 * @param timestamp 时间戳，为null则返回空字符串
	 * @return 格式化后的时间戳
	 * @since 1.0.0
	 */
	public static String formatDate(final Long timestamp) {
		if (Objects.isNull(timestamp)) {
			return StringUtils.EMPTY;
		}
		return format(timestamp, Constants.DATE_FORMAT);
	}

	/**
	 * 使用{@link Constants#TIME_FORMAT}格式化时间
	 *
	 * @param date 时间，为null则返回空字符串
	 * @return 格式化后的时间
	 * @since 1.0.0
	 */
	public static String formatTime(final Date date) {
		if (Objects.isNull(date)) {
			return StringUtils.EMPTY;
		}
		return format(date, Constants.TIME_FORMAT);
	}

	/**
	 * 使用{@link Constants#TIME_FORMAT}格式化时间戳
	 *
	 * @param timestamp 时间戳，为null则返回空字符串
	 * @return 格式化后的时间戳
	 * @since 1.0.0
	 */
	public static String formatTime(final Long timestamp) {
		if (Objects.isNull(timestamp)) {
			return StringUtils.EMPTY;
		}
		return format(timestamp, Constants.TIME_FORMAT);
	}
}
