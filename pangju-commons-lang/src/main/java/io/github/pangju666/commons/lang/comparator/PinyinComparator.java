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

package io.github.pangju666.commons.lang.comparator;

import com.hankcs.hanlp.HanLP;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 基于拼音的字符串比较器（不可变实现）
 * <p>实现规则：
 * <ol>
 *   <li>null值具有最高优先级</li>
 *   <li>空字符串""次之</li>
 *   <li>空白字符串" "再次之</li>
 *   <li>其他字符串按拼音顺序排序</li>
 * </ol>
 *
 * <p>示例：
 * <pre>
 * 排序前：["天气如何", null, " ", ""]
 * 排序后：[null, "", " ", "天气如何"]
 * </pre>
 *
 * @author pangju666
 * @see com.hankcs.hanlp.HanLP#convertToPinyinString
 * @since 1.0.0
 */
public final class PinyinComparator implements Comparator<String> {
	private final String separator;

	/**
	 * 构建使用空格作为拼音分隔符的比较器
	 *
	 * @see #PinyinComparator(String) 带分隔符参数的构造器
	 * @since 1.0.0
	 */
	public PinyinComparator() {
		this.separator = StringUtils.SPACE;
	}

	/**
	 * 构建自定义拼音分隔符的比较器
	 *
	 * @param separator 拼音之间的分隔符（如："-"），用于连接多音字转换结果
	 * @since 1.0.0
	 */
	public PinyinComparator(String separator) {
		this.separator = separator;
	}

	/**
	 * 对字符串列表进行拼音排序（原地修改，使用默认空格分隔符）
	 *
	 * @param list 要排序的字符串列表（允许为null，null列表不会抛出异常）
	 * @since 1.0.0
	 */
	public static void order(List<String> list) {
		if (Objects.nonNull(list) && !list.isEmpty()) {
			list.sort(new PinyinComparator());
		}
	}

	/**
	 * 对字符串列表进行拼音排序（原地修改）
	 *
	 * @param list      要排序的字符串列表（允许为null，null列表不会抛出异常）
	 * @param separator 自定义拼音分隔符
	 * @since 1.0.0
	 */
	public static void order(List<String> list, String separator) {
		if (Objects.nonNull(list) && !list.isEmpty()) {
			list.sort(new PinyinComparator(separator));
		}
	}

	/**
	 * 对字符串数组进行拼音排序（原地修改，使用默认空格分隔符）
	 *
	 * @param array 要排序的字符串数组（允许为null，null数组不会抛出异常）
	 * @since 1.0.0
	 */
	public static void order(String[] array) {
		if (ArrayUtils.isNotEmpty(array)) {
			Arrays.sort(array, new PinyinComparator());
		}
	}

	/**
	 * 对字符串数组进行拼音排序（原地修改）
	 *
	 * @param array     要排序的字符串数组（允许为null，null数组不会抛出异常）
	 * @param separator 自定义拼音分隔符
	 * @since 1.0.0
	 */
	public static void order(String[] array, String separator) {
		if (ArrayUtils.isNotEmpty(array)) {
			Arrays.sort(array, new PinyinComparator(separator));
		}
	}

	/**
	 * 比较两个字符串的拼音顺序
	 *
	 * @param o1 第一个字符串
	 * @param o2 第二个字符串
	 * @return 比较结果：
	 * <ul>
	 *   <li>负数：o1排在o2前面</li>
	 *   <li>正数：o1排在o2后面</li>
	 *   <li>0：两者相等</li>
	 * </ul>
	 */
	@Override
	public int compare(String o1, String o2) {
		// 调用原生方法判断是否相等
		if (Objects.equals(o1, o2)) {
			return 0;
		}

		// 判断是否为null
		if (Objects.isNull(o1)) {
			return -1;
		}
		if (Objects.isNull(o2)) {
			return 1;
		}

		// 判断是否为空字符串
		if (o1.isEmpty()) {
			return -1;
		}
		if (o2.isEmpty()) {
			return 1;
		}

		// 判断是否为空白字符串
		if (o1.isBlank()) {
			return -1;
		}
		if (o2.isBlank()) {
			return 1;
		}

		// 判断是否为ascii可打印字符，不是则获取其拼音字符串表示
		String o1PinYin = StringUtils.isAsciiPrintable(o1) ? o1 :
			HanLP.convertToPinyinString(o1, separator, false);
		String o2PinYin = StringUtils.isAsciiPrintable(o2) ? o2 :
			HanLP.convertToPinyinString(o2, separator, false);
		return o1PinYin.compareTo(o2PinYin);
	}
}