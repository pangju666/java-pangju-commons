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
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 字符串拼音比较器<br/>
 * null 优先级最高，其次为""、" "<br/>
 * 例如：
 * <pre>
 *     排序前：
 *     "天气如何", null, " ", ""
 *     排序后：
 *     null, "", " ", "天气如何"
 * </pre>
 *
 * @author pangju666
 * @see com.hankcs.hanlp.HanLP#convertToPinyinString 
 * @since 1.0.0
 */
public final class PinyinComparator implements Comparator<String> {
	private final String separator;

	/**
	 * 构建一个拼音比较器，分隔符默认为" "
	 *
	 * @since 1.0.0
	 */
	public PinyinComparator() {
		this.separator = StringUtils.SPACE;
	}

	/**
	 * 使用分隔符构建一个拼音比较器
	 *
	 * @param separator 拼音分隔符
	 * @since 1.0.0
	 */
	public PinyinComparator(String separator) {
		this.separator = separator;
	}

	/**
	 * 对列表进行排序
	 *
	 * @param list 字符串列表
	 * @since 1.0.0
	 */
	public static void order(List<String> list) {
		list.sort(new PinyinComparator());
	}

	/**
	 * 对列表进行排序
	 *
	 * @param list 字符串列表
	 * @param separator 拼音分隔符
	 *
	 * @since 1.0.0
	 */
	public static void order(List<String> list, String separator) {
		list.sort(new PinyinComparator(separator));
	}

	/**
	 * 对数组进行排序
	 *
	 * @param array 字符串数组
	 *
	 * @since 1.0.0
	 */
	public static void order(String[] array) {
		Arrays.sort(array, new PinyinComparator());
	}

	/**
	 * 对数组进行排序
	 *
	 * @param array 字符串数组
	 * @param separator 拼音分隔符
	 *
	 * @since 1.0.0
	 */
	public static void order(String[] array, String separator) {
		Arrays.sort(array, new PinyinComparator(separator));
	}

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
		String o1PinYin = StringUtils.isAsciiPrintable(o1) ? o1 : HanLP.convertToPinyinString(o1, separator, false);
		String o2PinYin = StringUtils.isAsciiPrintable(o2) ? o2 : HanLP.convertToPinyinString(o2, separator, false);
		return o1PinYin.compareTo(o2PinYin);
	}
}
