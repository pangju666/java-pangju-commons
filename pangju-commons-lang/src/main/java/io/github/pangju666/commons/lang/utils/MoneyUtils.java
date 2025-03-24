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

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 货币金额处理工具类
 * <p>提供金额数字转中文大写等货币相关处理方法</p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class MoneyUtils {
	protected static final String[] fractions = {"角", "分"};
	protected static final String[] digits = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
	protected static final String[][] units = {{"元", "万", "亿"}, {"", "拾", "佰", "仟"}};
	protected static final String REGEX = "(零.)+";

	protected MoneyUtils() {
	}

	/**
	 * 将金额数字转换为中文大写形式
	 *
	 * @param money 要转换的金额数值（支持负数）
	 * @return 中文大写金额字符串（示例：负壹万贰仟叁佰肆拾伍元陆角柒分）
	 * @since 1.0.0
	 */
	public static String formatAsChinese(double money) {
		String head = money < 0 ? "负" : "";
		money = Math.abs(money);

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < fractions.length; i++) {
			// 优化double计算精度丢失问题
			BigDecimal nNum = BigDecimal.valueOf(money);
			BigDecimal decimal = new BigDecimal(10);
			BigDecimal scale = nNum.multiply(decimal).setScale(2, RoundingMode.HALF_EVEN);
			double d = scale.doubleValue();
			int digitIndex = (int) (Math.floor(d * Math.pow(10, i)) % 10);
			builder.append((digits[digitIndex] + fractions[i]).replaceAll(REGEX, ""));
		}
		if (builder.isEmpty()) {
			builder.append("整");
		}
		int integerPart = (int) Math.floor(money);

		for (int i = 0; i < units[0].length && integerPart > 0; i++) {
			StringBuilder pBuilder = new StringBuilder();
			for (int j = 0; j < units[1].length && money > 0; j++) {
				pBuilder.insert(0, digits[integerPart % 10] + units[1][j]);
				integerPart = integerPart / 10;
			}
			String p = pBuilder.toString()
				.replaceAll("(零.)*零$", "")
				.replaceAll("^$", "零");
			builder.insert(0, p + units[0][i]);
		}
		String tail = builder.toString()
			.replaceAll("(零.)*零元", "元")
			.replaceFirst(REGEX, "")
			.replaceAll(REGEX, "零")
			.replaceAll("^整$", "零元整");
		return head + tail;
	}
}
