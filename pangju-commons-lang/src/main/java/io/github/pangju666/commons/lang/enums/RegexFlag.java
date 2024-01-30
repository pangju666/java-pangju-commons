package io.github.pangju666.commons.lang.enums;

public enum RegexFlag {
	UNIX_LINES(java.util.regex.Pattern.UNIX_LINES),
	CASE_INSENSITIVE(java.util.regex.Pattern.CASE_INSENSITIVE),
	COMMENTS(java.util.regex.Pattern.COMMENTS),
	MULTILINE(java.util.regex.Pattern.MULTILINE),
	DOTALL(java.util.regex.Pattern.DOTALL),
	UNICODE_CASE(java.util.regex.Pattern.UNICODE_CASE),
	CANON_EQ(java.util.regex.Pattern.CANON_EQ);

	private final int value;

	RegexFlag(int value) {
		this.value = value;
	}

	/**
	 * @return flag value as defined in {@link java.util.regex.Pattern}
	 */
	public int getValue() {
		return value;
	}
}