package io.github.pangju666.commons.lang.comparator;

import com.hankcs.hanlp.HanLP;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.Objects;

public final class PinyinComparator implements Comparator<String> {
	private final String separator;

	public PinyinComparator() {
		this.separator = StringUtils.SPACE;
	}

	public PinyinComparator(String separator) {
		this.separator = separator;
	}

	@Override
	public int compare(String o1, String o2) {
		if (StringUtils.compare(o1, o2) == 0) {
			return 0;
		}
		if (Objects.isNull(o1)) {
			return 1;
		}
		if (Objects.isNull(o2)) {
			return -1;
		}
		String o1PinYin = HanLP.convertToPinyinString(o1, separator, true);
		String o2PinYin = HanLP.convertToPinyinString(o1, separator, true);
		return o1PinYin.compareTo(o2PinYin);
	}
}
