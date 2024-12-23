package io.github.pangju666.commons.lang.utils;

import io.github.pangju666.commons.lang.pool.Constants;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
	protected DateUtils() {
	}

	public static Date parseDate(final String str) {
		return parseDateOrDefault(str, null);
	}

	public static Date parseDateOrDefault(final String str, final Date defaultDate) {
		try {
			if (StringUtils.isBlank(str)) {
				return defaultDate;
			}
			return parseDate(str, Constants.DATE_FORMAT, Constants.DATETIME_FORMAT, Constants.TIME_FORMAT);
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

	public static Date toDate(final LocalDateTime localDateTime) {
		return toDate(localDateTime, null);
	}

	public static Date toDate(final LocalDateTime localDateTime, final Date defaultValue) {
		if (Objects.isNull(localDateTime)) {
			return defaultValue;
		}
		ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
		return Date.from(zdt.toInstant());
	}

	public static Date toDate(final LocalDate localDate) {
		return toDate(localDate, null);
	}

	public static Date toDate(final LocalDate localDate, final Date defaultValue) {
		if (Objects.isNull(localDate)) {
			return defaultValue;
		}
		LocalDateTime localDateTime = LocalDateTime.of(localDate, LocalTime.of(0, 0, 0));
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

	public static Long betweenMillis(final Date date1, final Date date2) {
		return Optional.ofNullable(between(date1, date2)).map(Duration::toMillis).orElse(null);
	}

	public static Long betweenSeconds(final Date date1, final Date date2) {
		return Optional.ofNullable(between(date1, date2)).map(Duration::toSeconds).orElse(null);
	}

	public static Long betweenMinutes(final Date date1, final Date date2) {
		return Optional.ofNullable(between(date1, date2)).map(Duration::toMinutes).orElse(null);
	}

	public static Long betweenHours(final Date date1, final Date date2) {
		return Optional.ofNullable(between(date1, date2)).map(Duration::toHours).orElse(null);
	}

	public static Long betweenDays(final Date date1, final Date date2) {
		return Optional.ofNullable(between(date1, date2)).map(Duration::toDays).orElse(null);
	}

	protected static Duration between(final Date date1, final Date date2) {
		if (ObjectUtils.anyNull(date1, date2)) {
			return null;
		}
		long amount = Math.abs(date1.getTime() - date2.getTime());
		return Duration.of(amount, ChronoUnit.MILLIS);
	}

	public static Integer truncateBetweenYears(final Date date1, final Date date2) {
		return truncateBetween(date1, date2, Calendar.YEAR);
	}

	public static Integer truncateBetweenMonths(final Date date1, final Date date2) {
		return truncateBetween(date1, date2, Calendar.MONTH);
	}

	public static Integer truncateBetweenDays(final Date date1, final Date date2) {
		return truncateBetween(date1, date2, Calendar.DATE);
	}

	public static Integer truncateBetweenHours(final Date date1, final Date date2) {
		return truncateBetween(date1, date2, Calendar.HOUR);
	}

	public static Integer truncateBetweenMinutes(final Date date1, final Date date2) {
		return truncateBetween(date1, date2, Calendar.MINUTE);
	}

	public static Integer truncateBetweenSeconds(final Date date1, final Date date2) {
		return truncateBetween(date1, date2, Calendar.SECOND);
	}

	public static Integer truncateBetween(final Date date1, final Date date2, int field) {
		if (ObjectUtils.anyNull(date1, date2)) {
			return null;
		}
		int amount1 = toCalendar(date1).get(field);
		int amount2 = toCalendar(date2).get(field);
		return Math.abs(amount1 - amount2);
	}

	public static Integer calculateAge(final Date birthDate) {
		if (Objects.isNull(birthDate)) {
			return null;
		}
		int nowYear = toCalendar(nowDate()).get(Calendar.YEAR);
		int birthYear = toCalendar(birthDate).get(Calendar.YEAR);
		return nowYear - birthYear;
	}
}
