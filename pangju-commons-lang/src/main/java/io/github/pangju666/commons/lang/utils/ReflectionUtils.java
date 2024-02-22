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
		Field field = getField(obj, fieldName);
		if (Objects.isNull(field)) {
			return null;
		}
		return getFieldValue(obj, field);
	}

	@SuppressWarnings("unchecked")
	public static <E> E getFieldValue(final Object obj, final Field field) {
		boolean accessible = isAccessible(field, obj);
		if (!accessible) {
			field.setAccessible(true);
		}
		try {
			E value = (E) field.get(obj);
			if (!accessible) {
				field.setAccessible(false);
			}
			return value;
		} catch (IllegalAccessException e) {
			ExceptionUtils.rethrow(e);
			return null;
		}
	}

	public static <E> void setFieldValue(final Object obj, final String fieldName, final E value) {
		Field field = getField(obj, fieldName);
		if (Objects.isNull(field)) {
			return;
		}
		setFieldValue(obj, field, value);
	}

	public static <E> void setFieldValue(final Object obj, final Field field, final E value) {
		boolean accessible = isAccessible(field, obj);
		if (!accessible) {
			field.setAccessible(true);
		}
		try {
			field.set(obj, value);
			if (!accessible) {
				field.setAccessible(false);
			}
		} catch (IllegalAccessException e) {
			ExceptionUtils.rethrow(e);
		}
	}

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
		if (!isAccessible(field, instance)) {
			field.setAccessible(true);
		}
	}

	public static void setAccessible(final Field field) {
		if (!isAccessible(field)) {
			field.setAccessible(true);
		}
	}

	public static boolean isAccessible(final Field field, final Object instance) {
		return (Modifier.isPublic(field.getModifiers()) || Modifier.isPublic(field.getDeclaringClass().getModifiers())
			|| !Modifier.isFinal(field.getModifiers())) && field.canAccess(instance);
	}

	public static boolean isAccessible(final Field field) {
		return Modifier.isPublic(field.getModifiers()) || Modifier.isPublic(field.getDeclaringClass().getModifiers())
			|| !Modifier.isFinal(field.getModifiers());
	}
}
