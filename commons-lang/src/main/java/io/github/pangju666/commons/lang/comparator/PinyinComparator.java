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
 * <p>比较优先级与规则：</p>
 * <ul>
 *   <li><b>null：</b> 优先级最高（排在最前）</li>
 *   <li><b>空字符串：</b> 次高（仅次于 null）</li>
 *   <li><b>空白字符串：</b> 再次之；当两者均为空白时按长度升序比较，长度相同视为相等</li>
 *   <li><b>其他字符串：</b>
 *     <ul>
 *       <li>若为 ASCII 可打印字符，按字典序比较</li>
 *       <li>否则使用 HanLP 转为无音调拼音字符串，并以分隔符连接后按字典序比较</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p>示例：</p>
 * <pre>
 * 排序前：["天气如何", null, " ", "", "  "]
 * 排序后：[null, "", " ", "  ", "天气如何"]
 * </pre>
 *
 * <p>说明：</p>
 * <ul>
 *   <li>分隔符用于连接多音字或多字的拼音结果，例如 "-" 或空格</li>
 *   <li>该比较器不可变且线程安全；不要求与 equals 一致</li>
 *   <li>可考虑 {@code Collator.getInstance(Locale.CHINA)} 进行区域化比较，但与拼音规则可能存在差异</li>
 * </ul>
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
	public PinyinComparator(final String separator) {
		this.separator = separator;
	}

	/**
	 * 对字符串列表进行拼音排序（原地修改，使用默认空格分隔符）
	 *
	 * @param list 要排序的字符串列表（允许为null，null列表不会抛出异常）
	 * @since 1.0.0
	 */
	public static void order(final List<String> list) {
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
	public static void order(final List<String> list, final String separator) {
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
	public static void order(final String[] array) {
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
	public static void order(final String[] array, final String separator) {
		if (ArrayUtils.isNotEmpty(array)) {
			Arrays.sort(array, new PinyinComparator(separator));
		}
	}

	/**
	 * 比较两个字符串的拼音顺序
	 *
	 * @param o1 第一个字符串
	 * @param o2 第二个字符串
	 * @return 比较结果（负数：o1 在前；正数：o1 在后；0：相等）。比较遵循以下顺序：
	 * <ol>
	 *   <li>null 优先；o1 为 null 则在前，o2 为 null 则在后</li>
	 *   <li>空字符串 "" 次之；o1 为空串则在前，o2 为空串则在后</li>
	 *   <li>空白字符串再次之；若两者均为空白，按长度升序比较（长度相同视为相等）</li>
	 *   <li>其他字符串：
	 *     <ul>
	 *       <li>ASCII 可打印字符按字典序比较</li>
	 *       <li>非 ASCII 字符使用 HanLP 转为拼音并按字典序比较</li>
	 *     </ul>
	 *   </li>
	 * </ol>
	 */
	@Override
	public int compare(String o1, String o2) {
		// 调用原生方法判断是否相等
		if (Objects.equals(o1, o2)) {
			return 0;
		}

		// 判断是否为 null
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

		// 判断是否为空白字符串；若两者均为空白，按长度升序比较（长度相同视为相等）
		boolean o1Blank = o1.isBlank();
		boolean o2Blank = o2.isBlank();
		if (o1Blank && o2Blank) {
			return o1.length() - o2.length();
		}
		if (o1Blank) {
			return -1;
		}
		if (o2Blank) {
			return 1;
		}

		// 判断是否为Ascii可打印字符，不是则获取其拼音字符串表示
		String o1PinYin = StringUtils.isAsciiPrintable(o1) ? o1 :
			HanLP.convertToPinyinString(o1, separator, false);
		String o2PinYin = StringUtils.isAsciiPrintable(o2) ? o2 :
			HanLP.convertToPinyinString(o2, separator, false);
		return o1PinYin.compareTo(o2PinYin);
	}
}