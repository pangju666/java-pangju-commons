package io.github.pangju666.commons.lang.utils;

import io.github.pangju666.commons.lang.pool.ConstantPool;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

public class ReflectionUtils extends org.reflections.ReflectionUtils {
	protected ReflectionUtils() {
	}

	@SuppressWarnings("unchecked")
	public static <E> E getFieldValue(final Object obj, final String fieldName) {
		Field field = getAccessibleField(obj, fieldName);
		if (Objects.isNull(field)) {
			return null;
		}
		try {
			return (E) field.get(obj);
		} catch (IllegalAccessException ignored) {
			return null;
		}
	}

	public static <E> void setFieldValue(final Object obj, final String fieldName, final E value) {
		Field field = getAccessibleField(obj, fieldName);
		if (Objects.isNull(field)) {
			return;
		}
		try {
			field.set(obj, value);
		} catch (IllegalAccessException ignored) {
		}
	}

	public static Field getAccessibleField(final Object obj, final String fieldName) {
		if (Objects.isNull(obj)) {
			return null;
		}
		for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
			try {
				Field field = superClass.getDeclaredField(fieldName);
				setAccessible(field, obj);
				return field;
			} catch (NoSuchFieldException ignored) {
			}
		}
		return null;
	}

	public static <T> String getClassName(final T t) {
		return getClassName(t.getClass());
	}

	public static String getClassName(final Class<?> clz) {
		return StringUtils.substringAfterLast(clz.getName(), ".");
	}

	public static <T> Class<T> getClassGenericType(final Class<?> clazz) {
		return getClassGenericType(clazz, 0);
	}

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

	public static Class<?> getUserClass(final Object instance) {
		Class<?> clazz = instance.getClass();
		if (clazz.getName().contains(ConstantPool.CGLIB_CLASS_SEPARATOR)) {
			Class<?> superClass = clazz.getSuperclass();
			if (Objects.nonNull(superClass) && !Object.class.equals(superClass)) {
				return superClass;
			}
		}
		return clazz;
	}

	public static void setAccessible(final Field field, final Object instance) {
		if ((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())
			|| Modifier.isFinal(field.getModifiers())) && !field.canAccess(instance)) {
			field.setAccessible(true);
		}
	}

	public static void setAccessible(final Field field) {
		if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())
			|| Modifier.isFinal(field.getModifiers())) {
			field.setAccessible(true);
		}
	}
}
