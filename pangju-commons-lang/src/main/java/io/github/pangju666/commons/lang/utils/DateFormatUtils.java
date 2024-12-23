package io.github.pangju666.commons.lang.utils;

import io.github.pangju666.commons.lang.pool.Constants;

import java.util.Date;
import java.util.Objects;

public class DateFormatUtils extends org.apache.commons.lang3.time.DateFormatUtils {
	protected DateFormatUtils() {
	}

	public static String formatDatetime() {
		return formatDatetime(new Date());
	}

	public static String formatDatetime(final Date date) {
		if (Objects.isNull(date)) {
			return StringUtils.EMPTY;
		}
		return format(date, Constants.DATETIME_FORMAT);
	}

	public static String formatDatetime(final Long timestamp) {
		if (Objects.isNull(timestamp)) {
			return StringUtils.EMPTY;
		}
		return format(timestamp, Constants.DATETIME_FORMAT);
	}

	public static String formatDate() {
		return formatDate(new Date());
	}

	public static String formatDate(final Date date) {
		if (Objects.isNull(date)) {
			return StringUtils.EMPTY;
		}
		return format(date, Constants.DATE_FORMAT);
	}

	public static String formatDate(final Long timestamp) {
		if (Objects.isNull(timestamp)) {
			return StringUtils.EMPTY;
		}
		return format(timestamp, Constants.DATE_FORMAT);
	}

	public static String formatTime(final Date date) {
		if (Objects.isNull(date)) {
			return StringUtils.EMPTY;
		}
		return format(date, Constants.TIME_FORMAT);
	}

	public static String formatTime(final Long timestamp) {
		if (Objects.isNull(timestamp)) {
			return StringUtils.EMPTY;
		}
		return format(timestamp, Constants.TIME_FORMAT);
	}
}
