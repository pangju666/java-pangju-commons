package io.github.pangju666.commons.lang.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class StringUtils extends org.apache.commons.lang3.StringUtils {
	protected StringUtils() {
	}

	public static String convertCharset(final String str, final Charset destCharset) {
		Validate.notNull(destCharset, "目标字符集不可为null");
		if (isBlank(str)) {
			return str;
		}
		return new String(str.getBytes(), destCharset);
	}

	public static String convertCharset(final String str, final Charset srcCharset, final Charset destCharset) {
		Validate.notNull(srcCharset, "原始字符集不可为null");
		Validate.notNull(destCharset, "目标字符集不可为null");
		if (isBlank(str) || srcCharset.equals(destCharset)) {
			return str;
		}
		return new String(str.getBytes(srcCharset), destCharset);
	}

	public static List<String> getNotBlankElements(final Collection<String> collection) {
		if (CollectionUtils.isEmpty(collection)) {
			return Collections.emptyList();
		}
		return collection.stream()
			.filter(StringUtils::isNotBlank)
			.toList();
	}

	public static List<String> getUniqueNotBlankElements(final Collection<String> collection) {
		if (CollectionUtils.isEmpty(collection)) {
			return Collections.emptyList();
		}
		return collection.stream()
			.filter(StringUtils::isNotBlank)
			.distinct()
			.toList();
	}
}
