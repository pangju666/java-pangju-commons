package io.github.pangju666.commons.lang.utils


import spock.lang.Specification

class ArrayUtilsSpec extends Specification {
	def "TestPartition"() {
		expect:
		ArrayUtils.partition(array as String[], 2) == result

		where:
		array                                              || result
		null                                               || [] as List<String[]>
		[]                                                 || [] as List<String[]>
		[null, "", "天气如何"]                             || [[null, ""], ["天气如何"]] as List<String[]>
		["天气如何", null, ""]                             || [["天气如何", null], [""]] as List<String[]>
		["天气如何", "测试字符串", "Best Java"]            || [["天气如何", "测试字符串"], ["Best Java"]] as List<String[]>
		["天气如何", "12312313", "Best Java"]              || [["天气如何", "12312313"], ["Best Java"]] as List<String[]>
		["天气如何???", "!!!!!测试字符串", "Best Java..."] || [["天气如何???", "!!!!!测试字符串"], ["Best Java..."]] as List<String[]>
	}

	def "TestIntPartition"() {
		expect:
		ArrayUtils.partition(array as int[], 2) == result

		where:
		array     || result
		null      || [] as List<int[]>
		[]        || [] as List<String[]>
		[1, 2, 3] || [[1, 2], [3]] as List<int[]>
	}

	def "TestLongPartition"() {
		expect:
		ArrayUtils.partition(array as long[], 2) == result

		where:
		array        || result
		null         || [] as List<long[]>
		[]           || [] as List<long[]>
		[1L, 2L, 3L] || [[1L, 2L], [3L]] as List<long[]>
	}

	def "TestFloatPartition"() {
		expect:
		ArrayUtils.partition(array as float[], 2) == result

		where:
		array        || result
		null         || [] as List<float[]>
		[]           || [] as List<float[]>
		[1f, 2f, 3f] || [[1f, 2f], [3f]] as List<float[]>
	}

	def "TestDoublePartition"() {
		expect:
		ArrayUtils.partition(array as double[], 2) == result

		where:
		array        || result
		null         || [] as List<double[]>
		[]           || [] as List<double[]>
		[1d, 2d, 3d] || [[1d, 2d], [3d]] as List<double[]>
	}

	def "TestShortPartition"() {
		expect:
		ArrayUtils.partition(array as short[], 2) == result

		where:
		array                                || result
		null                                 || [] as List<short[]>
		[]                                   || [] as List<short[]>
		[1 as short, 2 as short, 3 as short] || [[1 as short, 2 as short], [3 as short]] as List<short[]>
	}

	def "TestBytePartition"() {
		expect:
		ArrayUtils.partition(array as byte[], 2) == result

		where:
		array                             || result
		null                              || [] as List<byte[]>
		[]                                || [] as List<byte[]>
		[1 as byte, 2 as byte, 3 as byte] || [[1 as byte, 2 as byte], [3 as byte]] as List<byte[]>
	}

	def "TestCharPartition"() {
		expect:
		ArrayUtils.partition(array as char[], 2) == result

		where:
		array           || result
		null            || [] as List<char[]>
		[]              || [] as List<char[]>
		['a', 'b', 'c'] || [['a', 'b'], ['c']] as List<char[]>
	}

	def "TestBooleanPartition"() {
		expect:
		ArrayUtils.partition(array as boolean[], 2) == result

		where:
		array               || result
		null                || [] as List<boolean[]>
		[]                  || [] as List<boolean[]>
		[false, true, true] || [[false, true], [true]] as List<boolean[]>
	}
}
