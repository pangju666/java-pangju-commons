package io.github.pangju666.commons.lang.utils

import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Field
import java.lang.reflect.Method

class ReflectionUtilsSpec extends Specification {

	class TestClass {
		private String privateField = "privateValue"
		public String publicField = "publicValue"
		public static final String STATIC_FIELD = "staticValue"

		protected void test() {

		}
	}

	class GenericClass<T> {}

	class ConcreteClass extends GenericClass<String> {}

	def "测试字段值获取和设置"() {
		given:
		def obj = new TestClass()

		when: "获取公共字段值"
		def publicValue = ReflectionUtils.getFieldValue(obj, "publicField")

		then:
		publicValue == "publicValue"

		when: "获取私有字段值"
		def privateValue = ReflectionUtils.getFieldValue(obj, "privateField")

		then:
		privateValue == "privateValue"

		when: "设置字段值"
		ReflectionUtils.setFieldValue(obj, "privateField", "newValue")

		then:
		ReflectionUtils.getFieldValue(obj, "privateField") == "newValue"
	}

	@Unroll
	def "测试字段查找 - 字段名: #fieldName 预期存在: #expected"() {
		expect:
		(ReflectionUtils.getField(new TestClass(), fieldName) != null) == expected

		where:
		fieldName       | expected
		"publicField"   | true
		"privateField"  | true
		"nonExistField" | false
	}

	def "测试类名获取"() {
		expect:
		ReflectionUtils.getClassName(new TestClass()) == "ReflectionUtilsTest\$TestClass"
		ReflectionUtils.getClassName(TestClass.class) == "ReflectionUtilsTest\$TestClass"
	}

	def "测试泛型类型获取"() {
		given:
		def clazz = new ConcreteClass().getClass()

		when:
		def genericType = ReflectionUtils.getClassGenericType(clazz)

		then:
		genericType == String
	}

	@Unroll
	def "测试Object方法判断 - 方法: #methodName 预期结果: #expected"() {
		given:
		Method method = TestClass.getMethod(methodName as String, paramTypes as Class<?>[])

		expect:
		ReflectionUtils.isObjectMethod(method) == expected

		where:
		methodName | paramTypes     | expected
		"equals"   | [Object.class] | true
		"hashCode" | []             | true
		"toString" | []             | true
		"wait"     | [Long.TYPE]    | true
		"notify"   | []             | true
	}

	def "测试CGLIB重命名方法判断"() {
		given:
		Method method = new Object() {
			def CGLIB$test$0() {}
		}.getClass().getDeclaredMethod("CGLIB\$test\$0")

		expect:
		ReflectionUtils.isCglibRenamedMethod(method)
	}

	def "测试访问权限控制"() {
		given:
		Field field = TestClass.getDeclaredField("privateField")

		when: "检查默认访问权限"
		def accessible = ReflectionUtils.isAccessible(field, new TestClass())

		then:
		!accessible

		when: "修改访问权限"
		def success = ReflectionUtils.makeAccessible(field)

		then:
		success
		field.isAccessible()
	}

	def "测试静态字段识别"() {
		given:
		Field field = TestClass.getField("STATIC_FIELD")

		expect:
		ReflectionUtils.isPublicStaticFinal(field)
	}

	def "测试方法访问权限设置"() {
		given:
		Method method = TestClass.getDeclaredMethod("test")

		when:
		def success = ReflectionUtils.makeAccessible(method)

		then:
		success
		method.isAccessible()
	}
}