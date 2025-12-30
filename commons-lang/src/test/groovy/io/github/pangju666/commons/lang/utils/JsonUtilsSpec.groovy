package io.github.pangju666.commons.lang.utils

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.reflect.TypeToken
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant

class JsonUtilsSpec extends Specification {
	@Unroll
	def "测试parseString - 输入: '#input' 返回类型校验"() {
		expect:
		def el = JsonUtils.parseString(input)
		(el instanceof JsonNull) == isNull

		where:
		input       || isNull
		null        || true
		""          || true
		"   "       || true
		"{\"a\":1}" || false
		"[1,2,3]"   || false
	}

	@Unroll
	def "测试fromString(Class) 与 toString(Class) - 类型: #typeName"() {
		expect:
		JsonUtils.fromString(json, Map.class) == map
		JsonUtils.toString(map, Map.class) == new Gson().toJson(map, Map.class)

		where:
		typeName | json                    || map
		"Map"    | "{\"a\":1,\"b\":\"x\"}" || [a: 1, b: "x"]
	}

	@Unroll
	def "测试fromString(TypeToken) 与 toString(TypeToken) - 列表"() {
		given:
		def token = new TypeToken<List<Integer>>() {}

		expect:
		JsonUtils.fromString("[1,2,3]", token) == [1, 2, 3]
		JsonUtils.toString([1, 2, 3], token) == new Gson().toJson([1, 2, 3], token.type)
	}

	def "测试toString 空输入返回空字符串"() {
		expect:
		JsonUtils.toString(null as Map) == ""
	}

	@Unroll
	def "测试toJson 与 fromJson - 基本类型及空值"() {
		expect:
		JsonUtils.toJson(null).isJsonNull()
		JsonUtils.fromJson(JsonNull.INSTANCE, String.class) == null
		JsonUtils.toJson("abc").isJsonPrimitive()
		JsonUtils.fromJson(JsonUtils.toJson("abc"), String.class) == "abc"
	}

	def "测试集合序列化与反序列化 - JsonArray"() {
		given:
		JsonArray arr = new JsonArray()
		arr.add(1)
		arr.add(2)
		arr.add(3)

		expect:
		JsonUtils.fromJsonArray(arr) == [1, 2, 3]
		JsonUtils.fromJsonArray(new JsonArray()) == []
		JsonUtils.toJsonArray([1, 2, 3]).size() == 3
		JsonUtils.toJsonArray([], new Gson()).size() == 0

		and:
		def listToken = new TypeToken<List<Integer>>() {}
		JsonUtils.fromJsonArray(arr, listToken) == [1, 2, 3]
		JsonUtils.toJsonArray([1, 2, 3], new Gson(), listToken).size() == 3
	}

	def "测试DEFAULT_GSON 与 createGsonBuilder 的类型适配器"() {
		given:
		def gson = JsonUtils.createGsonBuilder().create()
		def nowDate = new Date(1735600000000L) // 固定时间戳
		def nowInstant = Instant.ofEpochMilli(1735600000000L)

		when:
		JsonElement dateEl = JsonUtils.toJson(nowDate, gson)
		JsonElement instantEl = JsonUtils.toJson(nowInstant, gson)

		then: "Date 与 Instant 序列化为毫秒时间戳（JsonPrimitive Number）"
		dateEl.isJsonPrimitive() && dateEl.getAsJsonPrimitive().isNumber()
		instantEl.isJsonPrimitive() && instantEl.getAsJsonPrimitive().isNumber()
		dateEl.getAsLong() == 1735600000000L
		instantEl.getAsLong() == 1735600000000L

		when: "反序列化数字到 Date/Instant"
		def d = JsonUtils.fromString("1735600000000", gson, Date.class)
		def ins = JsonUtils.fromString("1735600000000", gson, Instant.class)

		then:
		d.time == 1735600000000L
		ins.toEpochMilli() == 1735600000000L
	}

	def "测试BigIntegerTypeAdapter 与 BigDecimalTypeAdapter 序列化与反序列化"() {
		given:
		def gson = JsonUtils.createGsonBuilder().create()

		expect: "BigInteger 序列化为十进制字符串；非法字符串反序列化为 null；数字转 long 再转 BigInteger"
		JsonUtils.toJson(new BigInteger("12345678901234567890"), gson).getAsString() == "12345678901234567890"
		JsonUtils.fromString("\"not a number\"", gson, BigInteger.class) == null
		JsonUtils.fromString("123", gson, BigInteger.class) == new BigInteger("123")

		and: "BigDecimal 使用 toPlainString，避免科学计数法"
		JsonUtils.toJson(new BigDecimal("1E-10"), gson).getAsString() == "0.0000000001"
		JsonUtils.fromString("\"0.0001\"", gson, BigDecimal.class) == new BigDecimal("0.0001")
		JsonUtils.fromString("1.5", gson, BigDecimal.class) == BigDecimal.valueOf(1.5d)
	}
}
