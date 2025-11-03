package io.github.pangju666.commons.lang.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * MoneyUtils是一个金额工具类，提供：
 * <ul>
 *     <li>金额格式化（带千位分隔符，保留两位小数）</li>
 *     <li>阿拉伯数字金额转中文大写金额（支持负数、小数、亿级单位）</li>
 * </ul>
 *
 * <p>创意来自ruoyi</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * MoneyUtils.format(new BigDecimal("1234567.89"));
 * // 输出: 1,234,567.89
 *
 * MoneyUtils.convertToChinese(new BigDecimal("1234567.89"));
 * // 输出: 壹佰贰拾叁万肆仟伍佰陆拾柒元捌角玖分
 * }</pre>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class MoneyUtils {
	/**
	 * 中文数字表示
	 *
	 * @since 1.0.0
	 */
	protected static final String[] CN_NUMBERS = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
	/** 千位内单位（拾、佰、仟）
	 *
	 * @since 1.0.0
	 */
	protected static final String[] CN_UNITS = {"", "拾", "佰", "仟"};
	/**
	 * 小数单位（角、分）
	 *
	 * @since 1.0.0
	 */
	protected static final String[] CN_DECIMAL_UNITS = {"角", "分"};
	/** 百（用于提取角、分）
	 *
	 * @since 1.0.0
	 */
	protected static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
	/** 常量：整
	 *
	 * @since 1.0.0
	 */
	protected static final String ZHENG = "整";
	/** 常量：负
	 *
	 * @since 1.0.0
	 */
	protected static final String FU = "负";
	/**
	 * 常量：元
	 *
	 * @since 1.0.0
	 */
	protected static final String YUAN = "元";
	/**
	 * 线程安全的金额格式化器
	 * <p>{@code DecimalFormat} 不是线程安全的，因此这里使用 {@code ThreadLocal}</p>
	 *
	 * @since 1.0.0
	 */
	protected static final ThreadLocal<DecimalFormat> DECIMAL_FORMAT =
		ThreadLocal.withInitial(() -> new DecimalFormat("#,##0.00"));
	/** 大单位（万、亿）
	 *
	 * @since 1.0.0
	 */
	private static final String[] CN_BIG_UNITS = {"", "万", "亿"};

	protected MoneyUtils() {
	}

	/**
	 * 将 {@link Double} 金额格式化为带千位分隔符的字符串。
	 * <p>格式：#,##0.00</p>
	 *
	 * @param amount 金额（可为 {@code null}）
	 * @return 格式化后的金额字符串，若输入为 {@code null} 返回空字符串
	 * @since 1.0.0
	 * @see #format(BigDecimal)
	 */
	public static String format(final Double amount) {
		if (Objects.isNull(amount)) {
			return StringUtils.EMPTY;
		}
		return DECIMAL_FORMAT.get().format(BigDecimal.valueOf(amount));
	}

	/**
	 * 将 {@link BigDecimal} 金额格式化为带千位分隔符的字符串。
	 * <p>格式：#,##0.00</p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * MoneyUtils.format(new BigDecimal("1234567.89"));
	 * // 输出：1,234,567.89
	 * }</pre>
	 *
	 * @param amount 金额（可为 {@code null}）
	 * @return 格式化后的金额字符串，若输入为 {@code null} 返回空字符串
	 * @since 1.0.0
	 */
	public static String format(final BigDecimal amount) {
		if (Objects.isNull(amount)) {
			return StringUtils.EMPTY;
		}
		return DECIMAL_FORMAT.get().format(amount);
	}

	/**
	 * 将 {@link Double} 金额转换为中文大写金额。
	 *
	 * @param amount 金额（可为 {@code null}）
	 * @return 中文大写金额字符串
	 * @see #convertToChinese(BigDecimal)
	 * @since 1.0.0
	 */
	public static String convertToChinese(final Double amount) {
		if (Objects.isNull(amount)) {
			return StringUtils.EMPTY;
		}
		return convertToChinese(BigDecimal.valueOf(amount));
	}

	/**
	 * 将 {@link BigDecimal} 金额转换为中文大写金额。
	 * <p>支持：</p>
	 * <ul>
	 *     <li>负数（会加前缀“负”）</li>
	 *     <li>两位小数（角、分）</li>
	 *     <li>亿级金额</li>
	 * </ul>
	 *
	 * <p>规则：</p>
	 * <ul>
	 *     <li>整数部分按“亿-万-仟”分节处理</li>
	 *     <li>小数部分仅支持两位（四舍五入由调用方控制）</li>
	 *     <li>“0角5分”形式将显示为“零伍分”</li>
	 * </ul>
	 *
	 * <p>示例：</p>
	 * <pre>
	 * 1234567.89 → 壹佰贰拾叁万肆仟伍佰陆拾柒元捌角玖分
	 * -100200300.05 → 负壹亿零贰拾万零叁佰元零伍分
	 * 0 → 零元整
	 * </pre>
	 *
	 * @param amount 金额（可为 {@code null}）
	 * @return 中文大写金额字符串
	 * @since 1.0.0
	 */
	public static String convertToChinese(final BigDecimal amount) {
		if (Objects.isNull(amount)) {
			return StringUtils.EMPTY;
		}
		if (amount.compareTo(BigDecimal.ZERO) == 0) {
			return "零元整";
		}

		StringBuilder result = new StringBuilder();
		BigDecimal absAmount = amount.abs();

		long integerPart = absAmount.longValue();
		int decimalPart = absAmount
			.subtract(new BigDecimal(integerPart))
			.multiply(ONE_HUNDRED)
			.intValue();

		// 整数部分
		String integerStr = convertIntegerPartToChinese(integerPart);
		result.append(integerStr).append(YUAN);

		// 小数部分
		if (decimalPart == 0) {
			result.append(ZHENG);
		} else {
			result.append(convertDecimalPartToChinese(decimalPart));
		}

		if (amount.signum() < 0) {
			result.insert(0, FU);
		}

		return result.toString();
	}

	/**
	 * 将整数部分（最高到亿级）转换为中文大写形式。
	 * <p>规则：每四位为一节，节与节之间以“万”“亿”区分。</p>
	 *
	 * @param integerPart 整数部分
	 * @return 中文大写整数部分字符串
	 * @since 1.0.0
	 */
	protected static String convertIntegerPartToChinese(long integerPart) {
		if (integerPart == 0) {
			return CN_NUMBERS[0];
		}

		StringBuilder result = new StringBuilder();
		int sectionIndex = 0;
		boolean needZero = false;

		while (integerPart > 0) {
			int section = (int) (integerPart % 10000);
			if (section != 0) {
				String sectionChinese = convertSectionToChinese(section);
				if (sectionIndex > 0) {
					sectionChinese += CN_BIG_UNITS[sectionIndex];
				}
				if (needZero) {
					result.insert(0, CN_NUMBERS[0]);
					needZero = false;
				}
				result.insert(0, sectionChinese);
			} else {
				if (result.length() > 0 && !result.toString().startsWith(CN_NUMBERS[0])) {
					needZero = true;
				}
			}
			integerPart /= 10000;
			sectionIndex++;
		}

		// 结尾零去掉
		return result.toString()
			.replaceAll("零+", "零") // 多个零合并
			.replaceAll("零$", StringUtils.EMPTY);
	}

	/**
	 * 将千位内（四位）数字转换为中文。
	 * <p>如：3050 → 叁仟零伍拾</p>
	 *
	 * @param section 四位节
	 * @return 中文字符串
	 * @since 1.0.0
	 */
	protected static String convertSectionToChinese(int section) {
		StringBuilder sectionStr = new StringBuilder();
		int unitPos = 0;
		boolean zero = false;

		while (section > 0) {
			int digit = section % 10;
			if (digit == 0) {
				if (!zero && sectionStr.length() > 0) {
					sectionStr.insert(0, CN_NUMBERS[0]);
					zero = true;
				}
			} else {
				sectionStr.insert(0, CN_NUMBERS[digit] + CN_UNITS[unitPos]);
				zero = false;
			}
			unitPos++;
			section /= 10;
		}

		return sectionStr.toString();
	}

	/**
	 * 将小数部分（角、分）转换为中文大写。
	 * <p>示例：</p>
	 * <ul>
	 *     <li>10 → 壹角</li>
	 *     <li>05 → 零伍分</li>
	 *     <li>50 → 伍角</li>
	 * </ul>
	 *
	 * @param decimalPart 小数部分（整数，已乘以100）
	 * @return 中文字符串
	 * @since 1.0.0
	 */
	protected static String convertDecimalPartToChinese(int decimalPart) {
		if (decimalPart == 0) {
			return StringUtils.EMPTY;
		}

		int jiao = decimalPart / 10;
		int fen = decimalPart % 10;

		StringBuilder result = new StringBuilder();
		if (jiao == 0 && fen > 0) {
			result.append("零");
		}
		if (jiao > 0) {
			result.append(CN_NUMBERS[jiao]).append(CN_DECIMAL_UNITS[0]);
		}
		if (fen > 0) {
			result.append(CN_NUMBERS[fen]).append(CN_DECIMAL_UNITS[1]);
		}
		return result.toString();
	}
}
