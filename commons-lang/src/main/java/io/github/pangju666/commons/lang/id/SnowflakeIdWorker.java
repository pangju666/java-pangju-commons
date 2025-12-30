package io.github.pangju666.commons.lang.id;

import io.github.pangju666.commons.lang.concurrent.SystemClock;

import java.util.concurrent.atomic.AtomicLong;

/**
 * <h1>SnowflakeIdWorker（使用 ChatGpt 生成的）</h1>
 * <p>
 * 基于 Twitter Snowflake 算法的高性能、分布式全局唯一 ID 生成器。<br>
 * 生成的 ID 为 64 位 long 型整数，按时间递增且在全局范围内唯一。
 * </p>
 *
 * <h2>ID 组成结构（共 64 位）</h2>
 * <pre>
 *  0 | 时间戳（41 位） | 数据中心 ID（5 位） | 机器 ID（5 位） | 序列号（12 位）
 * </pre>
 * <ul>
 *   <li>最高位（1 位）：固定为 0，保留符号位</li>
 *   <li>时间戳（41 位）：相对于设定起始时间 {@link #epoch} 的毫秒数，可支持约 69 年</li>
 *   <li>数据中心 ID（5 位）：支持最多 32 个数据中心</li>
 *   <li>机器 ID（5 位）：支持每个数据中心最多 32 台机器</li>
 *   <li>序列号（12 位）：支持每毫秒生成 4096 个 ID</li>
 * </ul>
 *
 * <h2>特性</h2>
 * <ul>
 *   <li>线程安全，无锁设计（基于 CAS）</li>
 *   <li>每毫秒最多可生成 4096 个唯一 ID</li>
 *   <li>可容忍 ≤5ms 的系统时钟小幅回拨</li>
 *   <li>支持自定义起始时间戳（epoch）</li>
 * </ul>
 *
 * <h2>示例</h2>
 * <pre>{@code
 * SnowflakeIdWorker worker = new SnowflakeIdWorker(1, 1);
 * long id = worker.nextId();
 * System.out.println(id);
 * }</pre>
 *
 * @since 1.0.0
 */
public final class SnowflakeIdWorker {
	/**
	 * 序列号部分占用的位数（12 位）
	 */
	private static final int BIT_SEQUENCE = 12;
	/**
	 * 机器 ID 部分占用的位数（5 位）
	 */
	private static final int BIT_WORKER_ID = 5;
	/**
	 * 数据中心 ID 部分占用的位数（5 位）
	 */
	private static final int BIT_DATACENTER_ID = 5;

	/**
	 * 最大机器 ID 值（31）
	 */
	private static final long MAX_WORKER_ID = ~(-1L << BIT_WORKER_ID);
	/**
	 * 最大数据中心 ID 值（31）
	 */
	private static final long MAX_DATACENTER_ID = ~(-1L << BIT_DATACENTER_ID);
	/**
	 * 序列号最大值（4095）
	 */
	private static final long SEQUENCE_MASK = ~(-1L << BIT_SEQUENCE);

	/**
	 * 各部分的位移量
	 */
	private static final int SHIFT_WORKER_ID = BIT_SEQUENCE;
	private static final int SHIFT_DATACENTER_ID = BIT_SEQUENCE + BIT_WORKER_ID;
	private static final int SHIFT_TIMESTAMP = BIT_SEQUENCE + BIT_WORKER_ID + BIT_DATACENTER_ID;

	/**
	 * 默认起始时间戳（Twitter 的 Snowflake 初始值：2010-11-04）
	 */
	private static final long DEFAULT_EPOCH = 1288834974657L;

	/**
	 * 当前机器 ID（0 ~ 31）
	 */
	private final long workerId;

	/** 当前数据中心 ID（0 ~ 31） */
	private final long datacenterId;

	/**
	 * 起始时间戳（epoch），用于计算相对时间
	 */
	private final long epoch;

	/**
	 * 上一次生成 ID 的时间戳（毫秒）
	 */
	private final AtomicLong lastTimestamp = new AtomicLong(-1L);

	/** 当前毫秒内的序列号（0 ~ 4095） */
	private final AtomicLong sequence = new AtomicLong(0L);

	/**
	 * 使用默认起始时间戳（{@link #DEFAULT_EPOCH}）构造一个新的 Snowflake ID 生成器。
	 *
	 * @param workerId     机器 ID（取值范围：0~31）
	 * @param datacenterId 数据中心 ID（取值范围：0~31）
	 * @throws IllegalArgumentException 当 workerId 或 datacenterId 超出取值范围时抛出
	 * @since 1.0.0
	 */
	public SnowflakeIdWorker(long workerId, long datacenterId) {
		this(workerId, datacenterId, DEFAULT_EPOCH);
	}

	/**
	 * 使用自定义起始时间戳构造 Snowflake ID 生成器。
	 *
	 * @param workerId     机器 ID（取值范围：0~31）
	 * @param datacenterId 数据中心 ID（取值范围：0~31）
	 * @param epoch        起始时间戳（毫秒），应早于系统当前时间
	 * @throws IllegalArgumentException 当参数非法时抛出
	 * @since 1.0.0
	 */
	public SnowflakeIdWorker(long workerId, long datacenterId, long epoch) {
		if (workerId > MAX_WORKER_ID || workerId < 0) {
			throw new IllegalArgumentException("workerId 必须介于 0 和 " + MAX_WORKER_ID);
		}
		if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
			throw new IllegalArgumentException("datacenterId 必须介于 0 和 " + MAX_DATACENTER_ID);
		}
		this.workerId = workerId;
		this.datacenterId = datacenterId;
		this.epoch = epoch;
	}

	/**
	 * 生成下一个全局唯一的 64 位 ID（线程安全）。
	 * <p>采用无锁的 CAS 机制确保高并发场景下的唯一性与性能。</p>
	 *
	 * @return 唯一 ID（long 类型，按时间递增）
	 * @throws RuntimeException 当检测到系统时钟严重回拨（超过 5ms）时抛出
	 * @since 1.0.0
	 */
	public long nextId() {
		while (true) {
			long current = SystemClock.now();
			long last = lastTimestamp.get();

			if (current < last) {
				// 可选：增加小回拨容忍，避免测试失败
				long offset = last - current;
				if (offset <= 5) {
					// 等待回拨恢复
					try {
						Thread.sleep(offset);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					continue;
				}
				throw new RuntimeException("时钟向后移动。拒绝为 " + offset + "ms 生成 ID");
			}

			if (current == last) {
				long seq = sequence.incrementAndGet() & SEQUENCE_MASK;
				if (seq == 0) {
					// 序列号用尽，等待下一毫秒
					current = waitNextMillis(last);
					// 不在此处重置 sequence！
				}
				// 尝试锁定这个时间戳
				if (lastTimestamp.compareAndSet(last, current)) {
					if (seq == 0) {
						// 只有在这里才安全重置 sequence
						sequence.set(0);
						return buildId(current, 0);
					} else {
						return buildId(current, seq);
					}
				}
				// CAS 失败，重试
			} else {
				// 新毫秒
				if (lastTimestamp.compareAndSet(last, current)) {
					sequence.set(0);
					return buildId(current, 0);
				}
				// CAS 失败，重试
			}
		}
	}

	/**
	 * 将各部分数据拼装为 64 位 ID。
	 *
	 * @param timestamp 当前时间戳（毫秒）
	 * @param seq       序列号
	 * @return 拼装后的唯一 ID
	 * @since 1.0.0
	 */
	private long buildId(long timestamp, long seq) {
		return ((timestamp - epoch) << SHIFT_TIMESTAMP)
			| (datacenterId << SHIFT_DATACENTER_ID)
			| (workerId << SHIFT_WORKER_ID)
			| seq;
	}

	/**
	 * 等待直到下一毫秒，以保证生成的时间戳单调递增。
	 *
	 * @param lastTimestamp 上次生成 ID 的时间戳
	 * @return 当前毫秒级时间戳（必定大于 lastTimestamp）
	 * @since 1.0.0
	 */
	private long waitNextMillis(long lastTimestamp) {
		long timestamp;
		do {
			timestamp = SystemClock.now();
		} while (timestamp <= lastTimestamp);
		return timestamp;
	}
}
