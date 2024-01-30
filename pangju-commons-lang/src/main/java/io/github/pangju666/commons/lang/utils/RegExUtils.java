package io.github.pangju666.commons.lang.utils;

import io.github.pangju666.commons.lang.enums.RegexFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExUtils extends org.apache.commons.lang3.RegExUtils {
	protected RegExUtils() {
	}

	public static int computeFlags(final RegexFlag... regexFlags) {
		int flags = 0;
		for (RegexFlag regexFlag : regexFlags) {
			if (flags == 0) {
				flags = regexFlag.getValue();
			} else {
				flags |= regexFlag.getValue();
			}
		}
		return flags;
	}

	public static Pattern compile(final Pattern pattern) {
		return compile(pattern.pattern());
	}

	public static Pattern compile(final String regex) {
		return compile(regex, 0, false, false);
	}

	public static Pattern compile(final Pattern pattern, boolean matchStart, boolean matchEnd) {
		return compile(pattern.pattern(), matchStart, matchEnd);
	}

	public static Pattern compile(final String regex, boolean matchStart, boolean matchEnd) {
		return compile(regex, 0, matchStart, matchEnd);
	}

	public static Pattern compile(final Pattern pattern, int flags) {
		return compile(pattern.pattern(), flags);
	}

	public static Pattern compile(final String regex, int flags) {
		return compile(regex, flags, false, false);
	}

	public static Pattern compile(final Pattern pattern, int flags, boolean matchStart, boolean matchEnd) {
		return compile(pattern.pattern(), flags, matchStart, matchEnd);
	}

	public static Pattern compile(final String regex, int flags, boolean matchStart, boolean matchEnd) {
		return Pattern.compile((matchStart && !regex.startsWith("^") ? "^" : "") + regex + (matchEnd && !regex.endsWith("$") ? "$" : ""), flags);
	}

	public static boolean matches(final String pattern, final String str) {
		return Pattern.compile(pattern).matcher(str).matches();
	}

	public static boolean matches(final Pattern pattern, final String str) {
		return pattern.matcher(str).matches();
	}

	public static List<String> find(final String pattern, final String str) {
		return find(Pattern.compile(pattern), str);
	}

	public static List<String> find(final Pattern pattern, final String str) {
		List<String> result = new ArrayList<>();
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			result.add(matcher.group());
		}
		return result;
	}
}
