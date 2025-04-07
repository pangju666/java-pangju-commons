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

package io.github.pangju666.commons.lang.utils;

import io.github.pangju666.commons.lang.pool.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.*;
import java.util.Objects;

/**
 * 反射操作工具类，继承并扩展了{@link org.reflections.ReflectionUtils}的功能
 * <p>提供字段访问、方法处理、类信息获取等反射相关操作</p>
 * <p>创意来自ruoyi</p>
 *
 * @author pangju666
 * @see org.reflections.ReflectionUtils
 * @since 1.0.0
 */
public class ReflectionUtils extends org.reflections.ReflectionUtils {
	protected ReflectionUtils() {
	}

	/**
	 * 获取对象指定字段的值
	 *
	 * @param obj       目标对象实例
	 * @param fieldName 要获取的字段名称
	 * @param <E>       返回值类型
	 * @return 字段值，若字段不存在返回null
	 * @since 1.0.0
	 */
	public static <E> E getFieldValue(final Object obj, final String fieldName) {
		Field field = getField(obj, fieldName);
		if (Objects.isNull(field)) {
			return null;
		}
		return getFieldValue(obj, field);
	}

	/**
	 * 通过反射字段对象获取字段值
	 *
	 * @param obj   目标对象实例
	 * @param field 要访问的字段对象
	 * @param <E>   返回值类型
	 * @return 字段值，若访问失败返回null
	 * @since 1.0.0
	 */
	@SuppressWarnings("unchecked")
	public static <E> E getFieldValue(final Object obj, final Field field) {
		boolean accessible = isAccessible(field, obj);
		if (!accessible && !makeAccessible(field)) {
			return null;
		}
		try {
			E value = (E) field.get(obj);
			if (!accessible) {
				field.setAccessible(false);
			}
			return value;
		} catch (IllegalAccessException e) {
			ExceptionUtils.asRuntimeException(e);
			return null;
		}
	}

	/**
	 * 设置对象指定字段的值
	 *
	 * @param obj       目标对象实例
	 * @param fieldName 要设置的字段名称
	 * @param value     要设置的值
	 * @param <E>       值类型
	 * @since 1.0.0
	 */
	public static <E> void setFieldValue(final Object obj, final String fieldName, final E value) {
		Field field = getField(obj, fieldName);
		if (Objects.isNull(field)) {
			return;
		}
		setFieldValue(obj, field, value);
	}

	/**
	 * 通过反射字段对象设置字段值
	 *
	 * @param obj   目标对象实例
	 * @param field 要设置的字段对象
	 * @param value 要设置的值
	 * @param <E>   值类型
	 * @since 1.0.0
	 */
	public static <E> void setFieldValue(final Object obj, final Field field, final E value) {
		boolean accessible = isAccessible(field, obj);
		if (!accessible && !makeAccessible(field)) {
			return;
		}
		try {
			field.set(obj, value);
			if (!accessible) {
				field.setAccessible(false);
			}
		} catch (IllegalAccessException e) {
			ExceptionUtils.asRuntimeException(e);
		}
	}

	/**
	 * 获取对象包含指定字段的Field对象
	 *
	 * @param obj       目标对象实例
	 * @param fieldName 要查找的字段名称
	 * @return 找到的字段对象，未找到返回null
	 * @since 1.0.0
	 */
	public static Field getField(final Object obj, final String fieldName) {
		if (Objects.isNull(obj)) {
			return null;
		}
		for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
			try {
				return superClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException ignored) {
			}
		}
		return null;
	}

	/**
	 * 获取对象的简化类名
	 *
	 * @param t   目标对象实例
	 * @param <T> 对象类型
	 * @return 类名（不包含包路径）
	 * @since 1.0.0
	 */
	public static <T> String getClassName(final T t) {
		return getClassName(t.getClass());
	}

	/**
	 * 获取类的简化名称
	 *
	 * @param clz 目标类对象
	 * @return 类名（不包含包路径）
	 * @since 1.0.0
	 */
	public static String getClassName(final Class<?> clz) {
		return StringUtils.substringAfterLast(clz.getName(), ".");
	}

	/**
	 * 获取类泛型的第一个类型参数
	 *
	 * @param clazz 目标类对象
	 * @param <T>   泛型类型
	 * @return 泛型类型Class对象，无法获取时返回null
	 * @since 1.0.0
	 */
	public static <T> Class<T> getClassGenericType(final Class<?> clazz) {
		return getClassGenericType(clazz, 0);
	}

	/**
	 * 获取指定索引的类泛型参数类型
	 *
	 * @param clazz 目标类对象
	 * @param index 泛型参数索引
	 * @param <T>   泛型类型
	 * @return 泛型类型Class对象，无法获取时返回null
	 * @since 1.0.0
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getClassGenericType(final Class<?> clazz, final int index) {
		Type genType = clazz.getGenericSuperclass();
		if (!(genType instanceof ParameterizedType)) {
			return null;
		}
		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
		if (index >= params.length || index < 0) {
			return null;
		}
		if (!(params[index] instanceof Class)) {
			return null;
		}
		return (Class<T>) params[index];
	}

	/**
	 * 获取对象的真实类（处理CGLIB代理情况）
	 *
	 * @param instance 对象实例
	 * @return 去除CGLIB代理后的真实类
	 * @since 1.0.0
	 */
	public static Class<?> getUserClass(final Object instance) {
		Class<?> clazz = instance.getClass();
		if (clazz.getName().contains(Constants.CGLIB_CLASS_SEPARATOR)) {
			Class<?> superClass = clazz.getSuperclass();
			if (Objects.nonNull(superClass) && !Object.class.equals(superClass)) {
				return superClass;
			}
		}
		return clazz;
	}

	/**
	 * 判断是否为equals方法
	 *
	 * @param method 方法对象
	 * @return 当方法参数为Object且方法名为equals时返回true
	 * @since 1.0.0
	 */
	public static boolean isEqualsMethod(final Method method) {
		return (method != null && method.getParameterCount() == 1 && method.getName().equals("equals") &&
			method.getParameterTypes()[0] == Object.class);
	}

	/**
	 * 判断是否为hashCode方法
	 *
	 * @param method 方法对象
	 * @return 当方法无参数且方法名为hashCode时返回true
	 * @since 1.0.0
	 */
	public static boolean isHashCodeMethod(final Method method) {
		return (method != null && method.getParameterCount() == 0 && method.getName().equals("hashCode"));
	}

	/**
	 * 判断是否为toString方法
	 *
	 * @param method 方法对象
	 * @return 当方法无参数且方法名为toString时返回true
	 * @since 1.0.0
	 */
	public static boolean isToStringMethod(final Method method) {
		return (method != null && method.getParameterCount() == 0 && method.getName().equals("toString"));
	}

	/**
	 * 判断是否为Object类声明的方法
	 *
	 * @param method 方法对象
	 * @return 当方法属于Object类或为equals/hashCode/toString方法时返回true
	 * @since 1.0.0
	 */
	public static boolean isObjectMethod(final Method method) {
		return (method != null && (method.getDeclaringClass() == Object.class ||
			isEqualsMethod(method) || isHashCodeMethod(method) || isToStringMethod(method)));
	}

	/**
	 * 判断是否为CGLIB重命名方法
	 *
	 * @param renamedMethod 方法对象
	 * @return 当方法名符合CGLIB重命名模式时返回true
	 * @since 1.0.0
	 */
	public static boolean isCglibRenamedMethod(final Method renamedMethod) {
		String name = renamedMethod.getName();
		if (name.startsWith(Constants.CGLIB_RENAMED_METHOD_PREFIX)) {
			int i = name.length() - 1;
			while (i >= 0 && Character.isDigit(name.charAt(i))) {
				i--;
			}
			return (i > Constants.CGLIB_RENAMED_METHOD_PREFIX.length() && (i < name.length() - 1) && name.charAt(i) == '$');
		}
		return false;
	}

	/**
	 * 检查字段是否可访问
	 *
	 * @param field    字段对象
	 * @param instance 目标实例
	 * @return 当字段可访问时返回true
	 * @since 1.0.0
	 */
	public static boolean isAccessible(final Field field, final Object instance) {
		return field.canAccess(instance);
	}

	/**
	 * 判断字段是否为public static final修饰
	 *
	 * @param field 字段对象
	 * @return 当字段是public static final时返回true
	 * @since 1.0.0
	 */
	public static boolean isPublicStaticFinal(final Field field) {
		int modifiers = field.getModifiers();
		return (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers));
	}

	/**
	 * 强制设置字段可访问
	 *
	 * @param field 字段对象
	 * @return 当成功修改访问权限时返回true
	 * @since 1.0.0
	 */
	@SuppressWarnings("deprecation")
	public static boolean makeAccessible(final Field field) {
		if ((!Modifier.isPublic(field.getModifiers()) ||
			!Modifier.isPublic(field.getDeclaringClass().getModifiers()) ||
			Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
			field.setAccessible(true);
			return true;
		}
		return false;
	}

	/**
	 * 强制设置方法可访问
	 *
	 * @param method 方法对象
	 * @return 当成功修改访问权限时返回true
	 * @since 1.0.0
	 */
	@SuppressWarnings("deprecation")
	public static boolean makeAccessible(final Method method) {
		if ((!Modifier.isPublic(method.getModifiers()) ||
			!Modifier.isPublic(method.getDeclaringClass().getModifiers())) && !method.isAccessible()) {
			method.setAccessible(true);
			return true;
		}
		return false;
	}
}
