package io.github.pangju666.commons.lang.id;

import io.github.pangju666.commons.lang.concurrent.SystemClock;

/**
 * <p>
 * 雪花算法用于生成全局唯一的 64 位长整型 ID，具有高效性和分布式可用性。
 * ID 结构如下（每部分占用的位数）：
 * <pre>
 *  0 | 时间戳（41 位） | 数据中心 ID（5 位） | 机器 ID（5 位） | 序列号（12 位）
 * </pre>
 * <ul>
 *     <li>最高位（1 位）：始终为 0，符号位不使用</li>
 *     <li>时间戳（41 位）：相对于设定起始时间的毫秒数，可使用约 69 年</li>
 *     <li>数据中心 ID（5 位）：支持 32 个数据中心</li>
 *     <li>机器 ID（5 位）：支持每个数据中心 32 台机器</li>
 *     <li>序列号（12 位）：支持每毫秒生成 4096 个 ID</li>
 * </ul>
 * 由于时间戳部分是 41 位，因此 ID 生成不会溢出，适用于分布式系统的唯一 ID 生成。
 *
 * @apiNote Chat Gpt 生成的算法实现
 * @since 1.0.0
 */
public final class SnowflakeIdWorker {
	// 12位序列号
	private static final long SEQUENCE_BITS = 12L;
	// 5位的机器标识位
	private static final long WORKER_ID_BITS = 5L;
	// 5位的数据中心标识位
	private static final long DATACENTER_ID_BITS = 5L;
	// 每部分的最大值
	private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
	private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
	// 每部分向左的位移
	private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
	private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
	private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
	private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
	// 记录上一次生成ID的时间戳
	private static long LAST_TIMESTAMP = -1L;
	// 机器ID（0~31）
	private final long workerId;
	// 数据中心ID（0~31）
	private final long datacenterId;
	// 12位的序列号
	private long sequence = 0L;
	// 初始时间戳（用于计算相对时间）
	private long initTimestamp = 1288834974657L;

	/**
	 * 构造雪花ID生成器实例
	 *
	 * @param workerId     机器ID (0~31)
	 * @param datacenterId 数据中心ID (0~31)
	 */
	public SnowflakeIdWorker(final long workerId, final long datacenterId) {
		this.workerId = workerId;
		this.datacenterId = datacenterId;
	}

	/**
	 * 构造雪花ID生成器实例
	 *
	 * @param workerId     机器ID (0~31)
	 * @param datacenterId 数据中心ID (0~31)
	 * @param sequence     初始序列号
	 */
	public SnowflakeIdWorker(final long workerId, final long datacenterId, final long sequence) {
		this.workerId = workerId;
		this.datacenterId = datacenterId;
		this.sequence = sequence;
	}

	/**
	 * 构造雪花ID生成器实例
	 *
	 * @param workerId      机器ID (0~31)
	 * @param datacenterId  数据中心ID (0~31)
	 * @param sequence      初始序列号
	 * @param initTimestamp 自定义起始时间戳
	 */
	public SnowflakeIdWorker(final long workerId, final long datacenterId, final long sequence, final long initTimestamp) {
		// 校验workerId的合法性
		if (workerId > MAX_WORKER_ID || workerId < 0) {
			throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
		}
		if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
			throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", MAX_DATACENTER_ID));
		}

		this.workerId = workerId;
		this.datacenterId = datacenterId;
		this.sequence = sequence;
		this.initTimestamp = initTimestamp;
	}

	/**
	 * 生成下一个唯一ID（线程安全）
	 *
	 * @return 唯一ID
	 */
	public synchronized long nextId() {
		long timestamp = SystemClock.now();

		// 如果当前时间小于上一次生成ID的时间，说明发生了时钟回拨，拒绝生成ID
		if (timestamp < LAST_TIMESTAMP) {
			throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", LAST_TIMESTAMP - timestamp));
		}

		// 如果是同一毫秒内，则增加序列号
		if (LAST_TIMESTAMP == timestamp) {
			sequence = (sequence + 1) & SEQUENCE_MASK;
			// 序列号溢出，等待下一毫秒
			if (sequence == 0) {
				timestamp = tilNextMillis(LAST_TIMESTAMP);
			}
		} else {
			// 时间戳变化，序列号重置
			sequence = 0L;
		}

		LAST_TIMESTAMP = timestamp;

		// 生成ID：时间戳偏移 | 数据中心ID | 机器ID | 序列号
		return ((timestamp - initTimestamp) << TIMESTAMP_LEFT_SHIFT) |
			(datacenterId << DATACENTER_ID_SHIFT) |
			(workerId << WORKER_ID_SHIFT) |
			sequence;
	}

	/**
	 * 等待直到下一毫秒，以保证时间戳单调递增
	 *
	 * @param lastTimestamp 上次生成ID的时间戳
	 * @return 当前毫秒级时间戳
	 */
	private long tilNextMillis(long lastTimestamp) {
		long timestamp = SystemClock.now();
		while (timestamp <= lastTimestamp) {
			timestamp = SystemClock.now();
		}
		return timestamp;
	}
}
