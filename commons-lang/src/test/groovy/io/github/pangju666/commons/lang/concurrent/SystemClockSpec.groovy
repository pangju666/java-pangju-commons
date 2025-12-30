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

package io.github.pangju666.commons.lang.concurrent

import spock.lang.Specification
import spock.lang.Title

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@Title("SystemClock 单元测试")
class SystemClockSpec extends Specification {

	def "测试 now() 方法返回的时间准确性"() {
		when: "获取 SystemClock 时间与系统时间"
		long clockTime = SystemClock.now()
		long sysTime = System.currentTimeMillis()

		then: "两者误差应在合理范围内（例如 20ms）"
		// 考虑到线程调度和时钟更新频率（1ms），误差通常很小
		// 但为了防止 CI 环境抖动，设置一个宽松的阈值
		Math.abs(clockTime - sysTime) <= 20
	}

	def "测试 now() 方法随时间更新"() {
		given: "记录开始时间"
		long start = SystemClock.now()

		when: "休眠一段时间"
		Thread.sleep(50)
		long end = SystemClock.now()

		then: "时间应向前推进"
		end > start
		// 验证推进的时间量大致正确
		(end - start) >= 40
	}

	def "测试高并发下 now() 的一致性"() {
		given: "并发线程数"
		int threadCount = 100
		def service = Executors.newFixedThreadPool(threadCount)
		def latch = new CountDownLatch(threadCount)
		def times = new long[threadCount]

		when: "并发获取时间"
		(0..<threadCount).each { i ->
			service.submit {
				try {
					times[i] = SystemClock.now()
				} finally {
					latch.countDown()
				}
			}
		}
		latch.await()
		service.shutdown()

		then: "所有获取的时间都应有效（大于0）"
		times.every { it > 0 }

		and: "获取的时间应接近当前系统时间"
		long current = System.currentTimeMillis()
		times.every { Math.abs(it - current) < 1000 }
	}
}
