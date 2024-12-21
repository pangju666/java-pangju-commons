package io.github.pangju666.commons.lang.id;

import io.github.pangju666.commons.lang.concurrent.SystemClock;

public final class SnowflakeIdWorker {
	//序列号部分
	private static final long SEQUENCE_BITS = 12L;
	//5位的机器标识
	private static final long WORKER_ID_BITS = 5L;
	private static final long DATACENTER_ID_BITS = 5L;
	//每部分的最大值
	private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
	private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
	//每部分向左的位移
	private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
	private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
	private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
	private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
	private static long LAST_TIMESTAMP = -1L;
	//下面两个每个5位，加起来就是10位的机器标识
	private final long workerId;
	private final long datacenterId;
	//12位的序列号
	private long sequence = 0L;
	//设置一个时间初始值(这个用自己业务系统上线的时间)，单位毫秒
	private long initTimestamp = 1288834974657L;

	public SnowflakeIdWorker(long workerId, long datacenterId) {
		this.workerId = workerId;
		this.datacenterId = datacenterId;
	}

	public SnowflakeIdWorker(long workerId, long datacenterId, long sequence) {
		this.workerId = workerId;
		this.datacenterId = datacenterId;
		this.sequence = sequence;
	}

	public SnowflakeIdWorker(long workerId, long datacenterId, long sequence, long initTimestamp) {
		// sanity check for workerId
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

	public synchronized long nextId() {
		long timestamp = SystemClock.now();

		if (timestamp < LAST_TIMESTAMP) {
			throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds",
				LAST_TIMESTAMP - timestamp));
		}

		if (LAST_TIMESTAMP == timestamp) {
			sequence = (sequence + 1) & SEQUENCE_MASK;
			if (sequence == 0) {
				timestamp = tilNextMillis(LAST_TIMESTAMP);
			}
		} else {
			sequence = 0L;
		}

		LAST_TIMESTAMP = timestamp;
		return ((timestamp - initTimestamp) << TIMESTAMP_LEFT_SHIFT) |
			(datacenterId << DATACENTER_ID_SHIFT) |
			(workerId << WORKER_ID_SHIFT) |
			sequence;
	}

	private long tilNextMillis(long lastTimestamp) {
		long timestamp = SystemClock.now();
		while (timestamp <= lastTimestamp) {
			timestamp = SystemClock.now();
		}
		return timestamp;
	}
}
