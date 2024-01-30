package io.github.pangju666.commons.lang.comparator;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Comparator;
import java.util.Objects;

public final class PinyinComparator implements Comparator<String> {
	private final PinyinFormat pinyinFormat;
	private final String separator;

	public PinyinComparator() {
		this.pinyinFormat = PinyinFormat.WITHOUT_TONE;
		this.separator = "_";
	}

	public PinyinComparator(String separator) {
		this.pinyinFormat = PinyinFormat.WITHOUT_TONE;
		this.separator = separator;
	}

	public PinyinComparator(PinyinFormat pinyinFormat) {
		this.pinyinFormat = pinyinFormat;
		this.separator = "_";
	}

	public PinyinComparator(String separator, PinyinFormat pinyinFormat) {
		this.pinyinFormat = pinyinFormat;
		this.separator = separator;
	}

	@Override
	public int compare(String o1, String o2) {
		try {
			if (StringUtils.compare(o1, o2) == 0) {
				return 0;
			}
			if (Objects.isNull(o1)) {
				return 1;
			}
			if (Objects.isNull(o2)) {
				return -1;
			}
			String o1PinYin = PinyinHelper.convertToPinyinString(o1, separator, pinyinFormat);
			String o2PinYin = PinyinHelper.convertToPinyinString(o2, separator, pinyinFormat);
			return o1PinYin.compareTo(o2PinYin);
		} catch (PinyinException e) {
			return ExceptionUtils.rethrow(e);
		}
	}
}
