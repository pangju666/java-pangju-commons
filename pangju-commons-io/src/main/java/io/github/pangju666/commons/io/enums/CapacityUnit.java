package io.github.pangju666.commons.io.enums;

public enum CapacityUnit {
	BYTE(CapacityUnit.BYTE_SCALE),
	KB(CapacityUnit.KB_SCALE),
	MB(CapacityUnit.MB_SCALE),
	GB(CapacityUnit.GB_SCALE),
	TB(CapacityUnit.TB_SCALE),
	PB(CapacityUnit.PB_SCALE);

	private static final long RADIX = 1024L;
	private static final long BYTE_SCALE = 1L;
	private static final long KB_SCALE = BYTE_SCALE * RADIX;
	private static final long MB_SCALE = KB_SCALE * RADIX;
	private static final long GB_SCALE = MB_SCALE * RADIX;
	private static final long TB_SCALE = GB_SCALE * RADIX;
	private static final long PB_SCALE = TB_SCALE * RADIX;

	private final long scale;

	CapacityUnit(long scale) {
		this.scale = scale;
	}

	private static long cvt(long d, long dst, long src) {
		long r;
		long m;
		if (src == dst)
			return d;
		else if (src < dst)
			return d / (dst / src);
		else if (d > (m = Long.MAX_VALUE / (r = src / dst)))
			return Long.MAX_VALUE;
		else if (d < -m)
			return Long.MIN_VALUE;
		else
			return d * r;
	}

	public long convert(long sourceCapacity, CapacityUnit sourceUnit) {
		return switch (this) {
			case BYTE -> sourceUnit.toBYTE(sourceCapacity);
			case KB -> sourceUnit.toKB(sourceCapacity);
			case MB -> sourceUnit.toMB(sourceCapacity);
			case GB -> sourceUnit.toGB(sourceCapacity);
			case TB -> sourceUnit.toTB(sourceCapacity);
			case PB -> sourceUnit.toPB(sourceCapacity);
		};
	}

	public long toBYTE(long capacity) {
		return cvt(capacity, BYTE_SCALE, scale);
	}

	public long toKB(long capacity) {
		return cvt(capacity, KB_SCALE, scale);
	}

	public long toMB(long capacity) {
		return cvt(capacity, MB_SCALE, scale);
	}

	public long toGB(long capacity) {
		return cvt(capacity, GB_SCALE, scale);
	}

	public long toTB(long capacity) {
		return cvt(capacity, TB_SCALE, scale);
	}

	public long toPB(long capacity) {
		return cvt(capacity, PB_SCALE, scale);
	}
}
