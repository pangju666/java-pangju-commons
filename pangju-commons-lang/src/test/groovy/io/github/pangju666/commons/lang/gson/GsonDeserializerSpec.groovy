package io.github.pangju666.commons.lang.gson

import io.github.pangju666.commons.lang.model.TestData
import io.github.pangju666.commons.lang.utils.JsonUtils
import spock.lang.Specification


class GsonDeserializerSpec extends Specification {
	def "test deserialize"() {
		setup:
		def object = JsonUtils.fromString(
			"{\"value\":\"TEST\",\"date\":1743649073482,\"localDate\":1743649073482,\"localDateTime\":1743649073482}",
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
			"  \"value\": 123,\n" +
			"  \"date\": \"asdsad\",\n" +
			"  \"localDate\": \"asdsad\",\n" +
			"  \"localDateTime\": \"asdsad\"\n" +
			"}", TestData.class)
		println object
	}
}
