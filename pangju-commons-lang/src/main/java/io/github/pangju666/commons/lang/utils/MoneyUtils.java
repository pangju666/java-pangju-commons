package io.github.pangju666.commons.lang.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * 金额工具类，提供金额格式化和中文大写转换功能
 * <p>支持金额的格式化展示和转换为中文大写金额</p>
 * <p>创意来自ruoyi</p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class MoneyUtils {
	/**
	 * 定义数字对应的汉字
	 *
	 * @since 1.0.0
	 */
	protected static final String[] CN_NUMBERS = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
	/**
	 * 定义单位
	 *
	 * @since 1.0.0
	 */
	protected static final String[] CN_UNITS = {"", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟", "万"};
	/**
	 * 定义小数单位
	 *
	 * @since 1.0.0
	 */
	protected static final String[] CN_DECIMAL_UNITS = {"角", "分"};

	protected static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
	protected static final String LING_PATTERN = "零$";
	protected static final String ZHENG = "整";
	protected static final String FU = "负";
	protected static final String YI_WAN = "亿万";
	protected static final String YI = "亿";

	/**
	 * 定义格式化模式：#,##0.00 表示整数部分三位一分隔，小数部分保留两位
	 *
	 * @since 1.0.0
	 */
	protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");

	protected MoneyUtils() {
	}

	/**
	 * 格式化金额，使用 "#,##0.00" 格式（千位分隔符，保留2位小数）
	 *
	 * @param amount 待格式化的金额
	 * @return 格式化后的金额字符串，如果输入为null则返回空字符串
	 * @since 1.0.0
	 */
	public static String format(final Double amount) {
		if (Objects.isNull(amount)) {
			return StringUtils.EMPTY;
		}
		return DECIMAL_FORMAT.format(BigDecimal.valueOf(amount));
	}

	/**
	 * 格式化金额，使用 "#,##0.00" 格式（千位分隔符，保留2位小数）
	 *
	 * @param amount 待格式化的金额
	 * @return 格式化后的金额字符串，如果输入为null则返回空字符串
	 * @since 1.0.0
	 */
	public static String format(final BigDecimal amount) {
		if (Objects.isNull(amount)) {
			return StringUtils.EMPTY;
		}
		return DECIMAL_FORMAT.format(amount);
	}

	/**
	 * 将金额转换为中文大写形式
	 * <p>支持到亿级别的金额转换，包含负数处理</p>
	 * <p>小数部分最多支持到分（两位小数）</p>
	 *
	 * @param amount 待转换的金额
	 * @return 中文大写金额字符串，如果输入为null则返回空字符串
	 * @since 1.0.0
	 */
	public static String convertToChinese(final Double amount) {
		if (Objects.isNull(amount)) {
			return StringUtils.EMPTY;
		}
		return convertToChinese(BigDecimal.valueOf(amount));
	}

	/**
	 * 将金额转换为中文大写形式
	 * <p>支持到亿级别的金额转换，包含负数处理</p>
	 * <p>小数部分最多支持到分（两位小数）</p>
	 *
	 * @param amount 待转换的金额
	 * @return 中文大写金额字符串，如果输入为null则返回空字符串
	 * @since 1.0.0
	 */
	public static String convertToChinese(final BigDecimal amount) {
		if (Objects.isNull(amount)) {
			return StringUtils.EMPTY;
		}

		StringBuilder builder = new StringBuilder();

		// 分离整数部分和小数部分
		long integerPart = amount.longValue();
		// 转换整数部分
		builder.append(convertIntegerPartToChinese(Math.abs(integerPart)));
		int integerLength = builder.length();

		int decimalPart = amount.subtract(new BigDecimal(integerPart))
			.multiply(ONE_HUNDRED)
			.intValue();
		// 转换小数部分
		builder.append(convertDecimalPartToChinese(Math.abs(decimalPart)));

		// 组合结果
		if (builder.length() == integerLength) {
			builder.append(ZHENG);
		}
		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			builder.insert(0, FU);
		}
		return builder.toString();
	}

	/**
	 * 将整数部分转换为中文大写
	 * <p>处理整数部分的中文转换，包含单位处理和零的处理</p>
	 *
	 * @param integerPart 整数部分
	 * @return 整数部分的中文大写字符串
	 * @since 1.0.0
	 */
	protected static String convertIntegerPartToChinese(long integerPart) {
		if (integerPart == 0) {
			return CN_NUMBERS[0];
		}

		StringBuilder result = new StringBuilder();
		int unitIndex = 0; // 当前单位索引
		boolean hasZero = false; // 是否需要插入“零”

		while (integerPart > 0) {
			int digit = (int) (integerPart % 10); // 获取当前位数字
			if (digit == 0) {
				hasZero = true;
			} else {
				if (hasZero) {
					result.insert(0, CN_NUMBERS[0]); // 插入“零”
					hasZero = false;
				}
				result.insert(0, CN_UNITS[unitIndex]); // 插入单位
				result.insert(0, CN_NUMBERS[digit]); // 插入数字
			}
			integerPart /= 10;
			unitIndex++;
		}

		// 处理多余的“零”和单位
		return result.toString()
			.replaceAll(LING_PATTERN, StringUtils.EMPTY)
			.replaceAll(YI_WAN, YI);
	}

	/**
	 * 将小数部分转换为中文大写
	 * <p>处理小数部分的中文转换，最多支持角分两位</p>
	 *
	 * @param decimalPart 小数部分（乘以100后的整数值）
	 * @return 小数部分的中文大写字符串
	 * @since 1.0.0
	 */
	protected static String convertDecimalPartToChinese(int decimalPart) {
		if (decimalPart == 0) {
			return StringUtils.EMPTY;
		}

		StringBuilder result = new StringBuilder();
		int jiao = decimalPart / 10; // 角
		int fen = decimalPart % 10; // 分

		if (jiao > 0) {
			result.append(CN_NUMBERS[jiao]).append(CN_DECIMAL_UNITS[0]);
		}
		if (fen > 0) {
			result.append(CN_NUMBERS[fen]).append(CN_DECIMAL_UNITS[1]);
		}

		return result.toString();
	}
}
