package io.github.pangju666.commons.lang.utils;

import io.github.pangju666.commons.lang.pool.ConstantPool;

import java.util.Date;

public class DateFormatUtils extends org.apache.commons.lang3.time.DateFormatUtils {
	protected DateFormatUtils() {
	}

	public static String formatDatetime() {
		return formatDatetime(new Date());
	}

	public static String formatDatetime(final Date date) {
		return format(date, ConstantPool.DATETIME_FORMAT);
	}

	public static String formatDatetime(long timestamp) {
		return format(timestamp, ConstantPool.DATETIME_FORMAT);
	}

	public static String formatDate() {
		return formatDate(new Date());
	}

	public static String formatDate(final Date date) {
		return format(date, ConstantPool.DATE_FORMAT);
	}

	public static String formatDate(long timestamp) {
		return format(timestamp, ConstantPool.DATE_FORMAT);
	}

	public static String formatTime(final Date date) {
		return format(date, ConstantPool.TIME_FORMAT);
	}

	public static String formatTime(long timestamp) {
		return format(timestamp, ConstantPool.TIME_FORMAT);
	}
}
