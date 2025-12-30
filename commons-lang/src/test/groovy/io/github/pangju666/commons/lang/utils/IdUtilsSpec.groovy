package io.github.pangju666.commons.lang.utils

import spock.lang.Specification
import spock.lang.Unroll

class IdUtilsSpec extends Specification {
	@Unroll
	def "UUID 格式与版本: 方法 #methodName"() {
		when:
		def s = supplier()
		def uuid = UUID.fromString(s)

		then:
		uuid.version() == 4
		uuid.variant() == 2

		where:
		methodName   | supplier
		"randomUUID" | { -> IdUtils.randomUUID() }
		"fastUUID"   | { -> IdUtils.fastUUID() }
	}

	@Unroll
	def "UUID 紧凑格式: 方法 #methodName"() {
		when:
		def s = supplier()

		then:
		s ==~ /[0-9a-fA-F]{32}/

		where:
		methodName         | supplier
		"simpleRandomUUID" | { -> IdUtils.simpleRandomUUID() }
		"simpleFastUUID"   | { -> IdUtils.simpleFastUUID() }
	}

	def "UUID 唯一性"() {
		expect:
		IdUtils.randomUUID() != IdUtils.randomUUID()
		IdUtils.simpleRandomUUID() != IdUtils.simpleRandomUUID()
		IdUtils.fastUUID() != IdUtils.fastUUID()
		IdUtils.simpleFastUUID() != IdUtils.simpleFastUUID()
	}

	def "ObjectId 格式与唯一性"() {
		when:
		def a = IdUtils.objectId()
		def b = IdUtils.objectId()

		then:
		a ==~ /[0-9a-f]{24}/
		b ==~ /[0-9a-f]{24}/
		a != b
	}

	@Unroll
	def "NanoId 默认与长度: size=#size"() {
		when:
		def s = size == null ? IdUtils.nanoId() : IdUtils.nanoId(size)

		then:
		s.length() == (size ?: 21)
		s ==~ /[A-Za-z0-9_-]{${size ?: 21}}/

		where:
		size << [null, 1, 10, 64]
	}

	def "NanoId 非法长度"() {
		when:
		IdUtils.nanoId(0)

		then:
		thrown(IllegalArgumentException)
	}
}
