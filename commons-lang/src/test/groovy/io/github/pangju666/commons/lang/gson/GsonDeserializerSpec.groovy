package io.github.pangju666.commons.lang.gson

import io.github.pangju666.commons.lang.model.TestData
import io.github.pangju666.commons.lang.utils.JsonUtils
import spock.lang.Specification


class GsonDeserializerSpec extends Specification {
	def "test deserialize"() {
		setup:
		def object = JsonUtils.fromString(
			"{\n" +
				"  \"phoneNumber\": \"133******26\",\n" +
				"  \"value\": \"TEST\",\n" +
				"  \"date\": 1743650529937,\n" +
				"  \"localDate\": 1743609600000,\n" +
				"  \"localDateTime\": 1743650529948,\n" +
				"  \"decimal\": 10002.00,\n" +
				"  \"aClass\": \"io.github.pangju666.commons.lang.model.TestData\"\n" +
				"}",
			TestData.class)
		println object
	}

	def "test deserialize null"() {
		setup:
		def object = JsonUtils.fromString(
			"{\"value\":null,\"date\":null,\"localDate\":null,\"localDateTime\":null}",
			TestData.class)
		println object
	}

	def "test deserialize error"() {
		setup:
		def object = JsonUtils.fromString("{\n" +
			"  \"phoneNumber\": \"133******26\",\n" +
			"  \"value\": \"TEST\",\n" +
			"  \"date\": 1743650529937,\n" +
			"  \"localDate\": 1743609600000,\n" +
			"  \"localDateTime\": 1743650529948,\n" +
			"  \"decimal\": \"1000aaa2.0aa0\",\n" +
			"  \"aClass\": \"ioa.github.pangju666.framework.core.model.TestData\"\n" +
			"}", TestData.class)
		println object
	}
}
