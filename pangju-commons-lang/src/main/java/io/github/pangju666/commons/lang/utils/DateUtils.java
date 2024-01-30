package io.github.pangju666.commons.lang.utils;

import io.github.pangju666.commons.lang.pool.ConstantPool;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
	protected DateUtils() {
	}

	public static Date parseDate(final String str) throws ParseException {
		return parseDate(str, ConstantPool.DATE_FORMAT, ConstantPool.DATETIME_FORMAT, ConstantPool.TIME_FORMAT);
	}

	public static Date parseDateOrDefault(final String str, final Date defaultDate) {
		try {
			if (StringUtils.isBlank(str)) {
				return defaultDate;
			}
			return parseDate(str, ConstantPool.DATE_FORMAT, ConstantPool.DATETIME_FORMAT, ConstantPool.TIME_FORMAT);
		} catch (ParseException e) {
			return defaultDate;
		}
	}

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

	public static Date nowDate() {
		return new Date();
	}

	public static Date toDate(final Long timestamp) {
		return toDate(timestamp, null);
	}

	public static Date toDate(final Long timestamp, final Date defaultValue) {
		if (Objects.isNull(timestamp)) {
			return defaultValue;
		}
		return new Date(timestamp);
	}

	public static Date toDate(final LocalDateTime temporalAccessor) {
		return toDate(temporalAccessor, null);
	}

	public static Date toDate(final LocalDateTime temporalAccessor, final Date defaultValue) {
		if (Objects.isNull(temporalAccessor)) {
			return defaultValue;
		}
		ZonedDateTime zdt = temporalAccessor.atZone(ZoneId.systemDefault());
		return Date.from(zdt.toInstant());
	}

	public static Date toDate(final LocalDate temporalAccessor) {
		return toDate(temporalAccessor, null);
	}

	public static Date toDate(final LocalDate temporalAccessor, final Date defaultValue) {
		if (Objects.isNull(temporalAccessor)) {
			return defaultValue;
		}
		LocalDateTime localDateTime = LocalDateTime.of(temporalAccessor, LocalTime.of(0, 0, 0));
		ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
		return Date.from(zdt.toInstant());
	}

	public static LocalDateTime toLocalDateTime(final Date date) {
		return toLocalDateTime(date, null);
	}

	public static LocalDateTime toLocalDateTime(final Date date, final LocalDateTime defaultValue) {
		if (Objects.isNull(date)) {
			return defaultValue;
		}
		return date.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();
	}

	public static LocalDateTime toLocalDateTime(final Long timestamp) {
		return toLocalDateTime(timestamp, null);
	}

	public static LocalDateTime toLocalDateTime(final Long timestamp, final LocalDateTime defaultValue) {
		if (Objects.isNull(timestamp)) {
			return defaultValue;
		}
		return new Date(timestamp)
				.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();
	}

	public static LocalDate toLocalDate(final Date date) {
		return toLocalDate(date, null);
	}

	public static LocalDate toLocalDate(final Date date, final LocalDate defaultValue) {
		if (Objects.isNull(date)) {
			return defaultValue;
		}
		return date.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
	}

	public static LocalDate toLocalDate(final Long timestamp) {
		return toLocalDate(timestamp, null);
	}

	public static LocalDate toLocalDate(final Long timestamp, final LocalDate defaultValue) {
		if (Objects.isNull(timestamp)) {
			return defaultValue;
		}
		return new Date(timestamp)
				.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
	}

	public static Long getTime(final Date date) {
		return getTime(date, null);
	}

	public static Long getTime(final Date date, final Long defaultValue) {
		if (Objects.isNull(date)) {
			return defaultValue;
		}
		return date.getTime();
	}

	public static long betweenMillis(final Date date1, final Date date2) {
		return between(date1, date2).toMillis();
	}

	public static long betweenSeconds(final Date date1, final Date date2) {
		return between(date1, date2).toSeconds();
	}

	public static long betweenMinutes(final Date date1, final Date date2) {
		return between(date1, date2).toMinutes();
	}

	public static long betweenHours(final Date date1, final Date date2) {
		return between(date1, date2).toHours();
	}

	public static long betweenDays(final Date date1, final Date date2) {
		return between(date1, date2).toDays();
	}

	protected static Duration between(final Date date1, final Date date2) {
		long amount = Math.abs(date1.getTime() - date2.getTime());
		return Duration.of(amount, ChronoUnit.MILLIS);
	}

	public static long truncateBetweenYears(final Date date1, final Date date2) {
		return truncateBetween(date1, date2, Calendar.YEAR);
	}


	public static long truncateBetweenMonths(final Date date1, final Date date2) {
		return truncateBetween(date1, date2, Calendar.MONTH);
	}


	public static long truncateBetweenDays(final Date date1, final Date date2) {
		return truncateBetween(date1, date2, Calendar.DATE);
	}


	public static long truncateBetweenHours(final Date date1, final Date date2) {
		return truncateBetween(date1, date2, Calendar.HOUR);
	}

	public static long truncateBetweenMinutes(final Date date1, final Date date2) {
		return truncateBetween(date1, date2, Calendar.MINUTE);
	}

	public static long truncateBetweenSeconds(final Date date1, final Date date2) {
		return truncateBetween(date1, date2, Calendar.SECOND);
	}

	public static int truncateBetween(final Date date1, final Date date2, int field) {
		int amount1 = toCalendar(date1).get(field);
		int amount2 = toCalendar(date2).get(field);
		return Math.abs(amount1 - amount2);
	}

	public static int calculateAge(final Date birth) {
		int nowYear = toCalendar(nowDate()).get(Calendar.YEAR);
		int birthYear = toCalendar(birth).get(Calendar.YEAR);
		return nowYear - birthYear;
	}
}
