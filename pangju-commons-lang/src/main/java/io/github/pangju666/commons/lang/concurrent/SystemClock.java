package io.github.pangju666.commons.lang.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 高并发场景下System.currentTimeMillis()的性能问题的优化
 *
 * <p>System.currentTimeMillis()的调用比new一个普通对象要耗时的多（具体耗时高出多少我还没测试过，有人说是100倍左右）</p>
 * <p>System.currentTimeMillis()之所以慢是因为去跟系统打了一次交道</p>
 * <p>后台定时更新时钟，JVM退出时，线程自动回收</p>
 * <p>10亿：43410,206,210.72815533980582%</p>
 * <p>1亿：4699,29,162.0344827586207%</p>
 * <p>1000万：480,12,40.0%</p>
 * <p>100万：50,10,5.0%</p>
 *
 * <p> copy from com.baomidou:mybatis-plus:core#com.baomidou.mybatisplus.core.toolkit.SystemClock version 3.5.4.1</p>
 */
public class SystemClock {
	protected final long period;
	protected final AtomicLong now;

	protected SystemClock(long period) {
		this.period = period;
		this.now = new AtomicLong(System.currentTimeMillis());
		scheduleClockUpdating();
	}

	protected static SystemClock instance() {
		return InstanceHolder.INSTANCE;
	}

	public static long now() {
		return instance().now.get();
	}

	protected void scheduleClockUpdating() {
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
			Thread thread = new Thread(runnable, "System Clock");
			thread.setDaemon(true);
			return thread;
		});
		scheduler.scheduleAtFixedRate(() ->
			now.set(System.currentTimeMillis()), period, period, TimeUnit.MILLISECONDS);
	}

	private static class InstanceHolder {
		public static final SystemClock INSTANCE = new SystemClock(1);

		private InstanceHolder() {
		}
	}
}
