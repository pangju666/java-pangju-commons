package io.github.pangju666.commons.lang.utils;

import io.github.pangju666.commons.lang.pool.ConstantPool;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.*;
import java.util.Objects;

public class ReflectionUtils extends org.reflections.ReflectionUtils {
	protected ReflectionUtils() {
	}

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

	public static <E> void setFieldValue(final Object obj, final String fieldName, final E value) {
		Field field = getField(obj, fieldName);
		if (Objects.isNull(field)) {
			return;
		}
		setFieldValue(obj, field, value);
	}

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

	public static boolean isEqualsMethod(Method method) {
		return (method != null && method.getParameterCount() == 1 && method.getName().equals("equals") &&
			method.getParameterTypes()[0] == Object.class);
	}

	public static boolean isHashCodeMethod(Method method) {
		return (method != null && method.getParameterCount() == 0 && method.getName().equals("hashCode"));
	}

	public static boolean isToStringMethod(Method method) {
		return (method != null && method.getParameterCount() == 0 && method.getName().equals("toString"));
	}

	public static boolean isObjectMethod(Method method) {
		return (method != null && (method.getDeclaringClass() == Object.class ||
			isEqualsMethod(method) || isHashCodeMethod(method) || isToStringMethod(method)));
	}

	public static boolean isCglibRenamedMethod(Method renamedMethod) {
		String name = renamedMethod.getName();
		if (name.startsWith(ConstantPool.CGLIB_RENAMED_METHOD_PREFIX)) {
			int i = name.length() - 1;
			while (i >= 0 && Character.isDigit(name.charAt(i))) {
				i--;
			}
			return (i > ConstantPool.CGLIB_RENAMED_METHOD_PREFIX.length() && (i < name.length() - 1) && name.charAt(i) == '$');
		}
		return false;
	}

	public static boolean isAccessible(Field field, Object instance) {
		return field.canAccess(instance);
	}

	public static boolean isPublicStaticFinal(Field field) {
		int modifiers = field.getModifiers();
		return (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers));
	}

	@SuppressWarnings("deprecation")
	public static boolean makeAccessible(Field field) {
		if ((!Modifier.isPublic(field.getModifiers()) ||
			!Modifier.isPublic(field.getDeclaringClass().getModifiers()) ||
			Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
			field.setAccessible(true);
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public static boolean makeAccessible(Method method) {
		if ((!Modifier.isPublic(method.getModifiers()) ||
			!Modifier.isPublic(method.getDeclaringClass().getModifiers())) && !method.isAccessible()) {
			method.setAccessible(true);
			return true;
		}
		return false;
	}
}
