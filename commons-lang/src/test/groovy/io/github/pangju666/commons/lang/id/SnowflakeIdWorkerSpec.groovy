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

package io.github.pangju666.commons.lang.id

import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@Title("SnowflakeIdWorker 单元测试")
class SnowflakeIdWorkerSpec extends Specification {

	def "测试 nextId 生成唯一 ID"() {
		given:
		def worker = new SnowflakeIdWorker(1, 1)

		when:
		long id1 = worker.nextId()
		long id2 = worker.nextId()

		then:
		id1 > 0
		id2 > 0
		id2 > id1 // 单调递增
	}

	def "测试高并发下 ID 唯一性"() {
		given:
		int threadCount = 100
		int loopCount = 100
		def worker = new SnowflakeIdWorker(1, 1)
		def service = Executors.newFixedThreadPool(threadCount)
		def latch = new CountDownLatch(threadCount)
		def ids = Collections.synchronizedList([]) // 改用 List 保留所有 ID

		when:
		(0..<threadCount).each {
			service.submit {
				try {
					for (int i = 0; i < loopCount; i++) {
						ids.add(worker.nextId())
					}
				} finally {
					latch.countDown()
				}
			}
		}
		latch.await()
		service.shutdown()

		then:
		def uniqueIds = ids as Set
		if (ids.size() != uniqueIds.size()) {
			// 找出重复 ID
			def seen = new HashSet<Long>()
			def duplicates = ids.findAll { !seen.add(it) }
			println "发现 ${duplicates.size()} 个重复 ID: ${duplicates.take(5)}"
		}
		ids.size() == threadCount * loopCount
		uniqueIds.size() == threadCount * loopCount
	}

	@Unroll
	def "测试构造函数参数校验: workerId=#w, datacenterId=#d"() {
		when:
		new SnowflakeIdWorker(w, d)

		then:
		thrown(IllegalArgumentException)

		where:
		w  | d
		-1 | 0
		32 | 0
		0  | -1
		0  | 32
	}

	def "测试自定义 Epoch"() {
		given:
		long epoch = 1672531200000L // 2023-01-01
		def worker = new SnowflakeIdWorker(0, 0, epoch)

		when:
		long id = worker.nextId()
		// 解析 ID 获取时间戳部分 (右移 22 位)
		long timestamp = (id >> 22) + epoch

		then:
		timestamp >= epoch
		// 允许一定的执行时间误差，确保生成的 timestamp 接近当前时间
		Math.abs(timestamp - System.currentTimeMillis()) < 1000
	}

	def "测试不同 Worker/Datacenter 生成的 ID 不重复"() {
		given:
		def worker1 = new SnowflakeIdWorker(1, 1)
		def worker2 = new SnowflakeIdWorker(2, 1)

		when:
		long id1 = worker1.nextId()
		long id2 = worker2.nextId()

		then:
		// 解析 workerId (5 bits)
		// ID结构: 0 | timestamp(41) | datacenter(5) | worker(5) | sequence(12)
		// 右移 12 位得到 worker+datacenter+timestamp
		// 再 & 0x1F (5位掩码) 得到 workerId ???
		// 不，WorkerId 在 sequence 左边
		// sequence: 12 bits
		// worker: 5 bits (shift 12)
		// datacenter: 5 bits (shift 17)

		long workerId1 = (id1 >> 12) & 0x1F
		long workerId2 = (id2 >> 12) & 0x1F

		workerId1 == 1
		workerId2 == 2
		id1 != id2
	}
}
