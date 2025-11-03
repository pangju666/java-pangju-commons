package io.github.pangju666.commons.lang.gson

import io.github.pangju666.commons.lang.enums.TestEnum
import io.github.pangju666.commons.lang.model.TestData
import io.github.pangju666.commons.lang.utils.DateUtils
import io.github.pangju666.commons.lang.utils.JsonUtils
import spock.lang.Specification

class GsonSerializerSpec extends Specification {
	def "test serialize"() {
		setup:
		def object = new TestData()
		object.value = TestEnum.TEST
		object.phoneNumber = "13336111326"
		object.date = new Date()
		object.localDate = DateUtils.toLocalDate(new Date())
		object.localDateTime = DateUtils.toLocalDateTime(new Date())
		object.aClass = TestData.class
		object.decimal = BigDecimal.ONE

		def json = JsonUtils.toString(object)
		println json
	}

	def "test serialize null"() {
		setup:
		def object = new TestData()
		object.value = TestEnum.TEST
		object.phoneNumber = "1"
		object.date = null
		object.localDate = null
		object.localDateTime = null
		object.aClass = null

		def json = JsonUtils.toString(object)
		println json
	}
}
