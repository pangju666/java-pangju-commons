package io.github.pangju666.commons.lang.comparator;

import com.hankcs.hanlp.HanLP;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class PinyinComparator implements Comparator<String> {
	private final String separator;

	public PinyinComparator() {
		this.separator = StringUtils.SPACE;
	}

	public PinyinComparator(String separator) {
		this.separator = separator;
	}

	public static void order(List<String> list) {
		list.sort(new PinyinComparator());
	}

	public static void order(List<String> list, String separator) {
		list.sort(new PinyinComparator(separator));
	}

	public static void order(String[] array) {
		Arrays.sort(array, new PinyinComparator());
	}

	public static void order(String[] array, String separator) {
		Arrays.sort(array, new PinyinComparator(separator));
	}

	@Override
	public int compare(String o1, String o2) {
		if (StringUtils.compare(o1, o2) == 0) {
			return 0;
		}
		if (Objects.isNull(o1)) {
			return -1;
		}
		if (Objects.isNull(o2)) {
			return 1;
		}
		if (o1.isEmpty()) {
			return -1;
		}
		if (o2.isEmpty()) {
			return 1;
		}
		if (o1.isBlank()) {
			return -1;
		}
		if (o2.isBlank()) {
			return 1;
		}
		String o1PinYin = HanLP.convertToPinyinString(o1, separator, false);
		String o2PinYin = HanLP.convertToPinyinString(o2, separator, false);
		return o1PinYin.compareTo(o2PinYin);
	}
}
