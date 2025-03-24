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

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * JSON处理工具类
 * <p>基于Gson实现，提供JSON字符串与Java对象间的双向转换能力</p>
 *
 * @author pangju666
 * @see com.google.gson.Gson
 * @since 1.0.0
 */
public class JsonUtils {
	/**
	 * 默认Gson实例（带格式化输出）
	 *
	 * @since 1.0.0
	 */
	public static final Gson DEFAULT_GSON;

	static {
		DEFAULT_GSON = new GsonBuilder()
			.setPrettyPrinting()
			.create();
	}

	protected JsonUtils() {
	}

	/**
	 * 解析JSON字符串为JsonElement
	 *
	 * @param json JSON字符串
	 * @return 解析后的JsonElement（空输入返回JsonNull）
	 * @since 1.0.0
	 */
	public static JsonElement parseString(final String json) {
		if (StringUtils.isBlank(json)) {
			return JsonNull.INSTANCE;
		}
		return JsonParser.parseString(json);
	}

	/**
	 * 反序列化JSON字符串到指定类型对象（使用默认GSON）
	 *
	 * @param json   JSON字符串
	 * @param _class 目标类类型
	 * @param <T>    返回值类型
	 * @return 反序列化后的对象（空输入返回null）
	 * @since 1.0.0
	 */
	public static <T> T fromString(final String json, final Class<T> _class) {
		return fromString(json, DEFAULT_GSON, _class);
	}

	/**
	 * 反序列化JSON字符串到指定类型对象（自定义GSON）
	 *
	 * @param json   JSON字符串
	 * @param gson   自定义Gson实例
	 * @param _class 目标类类型
	 * @param <T>    返回值类型
	 * @return 反序列化后的对象（空输入返回null）
	 * @since 1.0.0
	 */
	public static <T> T fromString(final String json, final Gson gson, final Class<T> _class) {
		if (StringUtils.isBlank(json)) {
			return null;
		}
		return gson.fromJson(json, _class);
	}

	/**
	 * 反序列化JSON字符串到泛型对象（使用默认GSON）
	 *
	 * @param json      JSON字符串
	 * @param typeToken 类型标记
	 * @param <T>       返回值类型
	 * @return 反序列化后的对象（空输入返回null）
	 * @since 1.0.0
	 */
	public static <T> T fromString(final String json, final TypeToken<T> typeToken) {
		return fromString(json, DEFAULT_GSON, typeToken);
	}

	/**
	 * 反序列化JSON字符串到泛型对象（自定义GSON）
	 *
	 * @param json      JSON字符串
	 * @param gson      自定义Gson实例
	 * @param typeToken 类型标记
	 * @param <T>       返回值类型
	 * @return 反序列化后的对象（空输入返回null）
	 * @since 1.0.0
	 */
	public static <T> T fromString(final String json, final Gson gson, final TypeToken<T> typeToken) {
		if (StringUtils.isBlank(json)) {
			return null;
		}
		return gson.fromJson(json, typeToken.getType());
	}

	/**
	 * 序列化对象为JSON字符串（使用默认GSON）
	 *
	 * @param src 要序列化的对象
	 * @param <T> 对象类型
	 * @return JSON字符串（空输入返回空字符串）
	 * @since 1.0.0
	 */
	public static <T> String toString(final T src) {
		return toString(src, DEFAULT_GSON);
	}

	/**
	 * 序列化对象为JSON字符串（自定义GSON）
	 *
	 * @param src  要序列化的对象
	 * @param gson 自定义Gson实例
	 * @param <T>  对象类型
	 * @return JSON字符串（空输入返回空字符串）
	 * @since 1.0.0
	 */
	public static <T> String toString(final T src, final Gson gson) {
		if (Objects.isNull(src)) {
			return StringUtils.EMPTY;
		}
		return gson.toJson(src);
	}

	/**
	 * 序列化对象为JSON字符串（指定类型，使用默认GSON）
	 *
	 * @param src    要序列化的对象
	 * @param _class 目标类类型
	 * @param <T>    对象类型
	 * @return JSON字符串（空输入返回空字符串）
	 * @since 1.0.0
	 */
	public static <T> String toString(final T src, final Class<T> _class) {
		return toString(src, DEFAULT_GSON, _class);
	}

	/**
	 * 序列化对象为JSON字符串（指定类型，自定义GSON）
	 *
	 * @param src   要序列化的对象
	 * @param gson  自定义Gson实例
	 * @param _class 目标类类型
	 * @param <T>   对象类型
	 * @return JSON字符串（空输入返回空字符串）
	 * @since 1.0.0
	 */
	public static <T> String toString(final T src, final Gson gson, final Class<T> _class) {
		if (Objects.isNull(src)) {
			return StringUtils.EMPTY;
		}
		return gson.toJson(src, _class);
	}

	/**
	 * 序列化泛型对象为JSON字符串（使用默认GSON）
	 *
	 * @param src      要序列化的对象
	 * @param typeToken 类型标记
	 * @param <T>      对象类型
	 * @return JSON字符串（空输入返回空字符串）
	 * @since 1.0.0
	 */
	public static <T> String toString(final T src, final TypeToken<T> typeToken) {
		return toString(src, DEFAULT_GSON, typeToken);
	}

	/**
	 * 序列化泛型对象为JSON字符串（自定义GSON）
	 *
	 * @param src      要序列化的对象
	 * @param gson     自定义Gson实例
	 * @param typeToken 类型标记
	 * @param <T>      对象类型
	 * @return JSON字符串（空输入返回空字符串）
	 * @since 1.0.0
	 */
	public static <T> String toString(final T src, final Gson gson, final TypeToken<T> typeToken) {
		if (Objects.isNull(src)) {
			return StringUtils.EMPTY;
		}
		return gson.toJson(src, typeToken.getType());
	}

	/**
	 * 反序列化JsonElement到对象（使用默认GSON）
	 *
	 * @param json   JsonElement对象
	 * @param _class 目标类类型
	 * @param <T>    返回值类型
	 * @return 反序列化后的对象（空输入返回null）
	 * @since 1.0.0
	 */
	public static <T> T fromJson(final JsonElement json, final Class<T> _class) {
		return fromJson(json, DEFAULT_GSON, _class);
	}

	/**
	 * 反序列化JsonElement到对象（自定义GSON）
	 *
	 * @param json   JsonElement对象
	 * @param gson   自定义Gson实例
	 * @param _class 目标类类型
	 * @param <T>    返回值类型
	 * @return 反序列化后的对象（null、JsonNull输入返回null）
	 * @since 1.0.0
	 */
	public static <T> T fromJson(final JsonElement json, final Gson gson, final Class<T> _class) {
		if (Objects.isNull(json) || json.isJsonNull()) {
			return null;
		}
		return gson.fromJson(json, _class);
	}

	/**
	 * 反序列化JsonElement到泛型对象（使用默认GSON）
	 *
	 * @param json     JsonElement对象
	 * @param typeToken 类型标记
	 * @param <T>      返回值类型
	 * @return 反序列化后的对象（null、JsonNull输入返回null）
	 * @since 1.0.0
	 */
	public static <T> T fromJson(final JsonElement json, final TypeToken<T> typeToken) {
		return fromJson(json, DEFAULT_GSON, typeToken);
	}

	/**
	 * 反序列化JsonElement到泛型对象（自定义GSON）
	 *
	 * @param json     JsonElement对象
	 * @param gson     自定义Gson实例
	 * @param typeToken 类型标记
	 * @param <T>      返回值类型
	 * @return 反序列化后的对象（null、JsonNull输入返回null）
	 * @since 1.0.0
	 */
	public static <T> T fromJson(final JsonElement json, final Gson gson, final TypeToken<T> typeToken) {
		if (Objects.isNull(json) || json.isJsonNull()) {
			return null;
		}
		return gson.fromJson(json, typeToken.getType());
	}

	/**
	 * 序列化对象为JsonElement（使用默认GSON）
	 *
	 * @param src 要序列化的对象
	 * @param <T> 对象类型
	 * @return JsonElement对象（null输入返回JsonNull）
	 * @since 1.0.0
	 */
	public static <T> JsonElement toJson(final T src) {
		return toJson(src, DEFAULT_GSON);
	}

	/**
	 * 序列化对象为JsonElement（自定义GSON）
	 *
	 * @param src  要序列化的对象
	 * @param gson 自定义Gson实例
	 * @param <T> 对象类型
	 * @return JsonElement对象（null输入返回JsonNull）
	 * @since 1.0.0
	 */
	public static <T> JsonElement toJson(final T src, final Gson gson) {
		if (Objects.isNull(src)) {
			return JsonNull.INSTANCE;
		}
		return gson.toJsonTree(src);
	}

	/**
	 * 序列化对象为JsonElement（指定类型，使用默认GSON）
	 *
	 * @param src   要序列化的对象
	 * @param _class 目标类类型
	 * @param <T>   对象类型
	 * @return JsonElement对象（null输入返回JsonNull）
	 * @since 1.0.0
	 */
	public static <T> JsonElement toJson(final T src, final Class<T> _class) {
		return toJson(src, DEFAULT_GSON, _class);
	}

	/**
	 * 序列化对象为JsonElement（指定类型，自定义GSON）
	 *
	 * @param src   要序列化的对象
	 * @param gson  自定义Gson实例
	 * @param _class 目标类类型
	 * @param <T>   对象类型
	 * @return JsonElement对象（null输入返回JsonNull）
	 * @since 1.0.0
	 */
	public static <T> JsonElement toJson(final T src, final Gson gson, final Class<T> _class) {
		if (Objects.isNull(src)) {
			return JsonNull.INSTANCE;
		}
		return gson.toJsonTree(src, _class);
	}

	/**
	 * 序列化泛型对象为JsonElement（使用默认GSON）
	 *
	 * @param src      要序列化的对象
	 * @param typeToken 类型标记
	 * @param <T>      对象类型
	 * @return JsonElement对象（null输入返回JsonNull）
	 * @since 1.0.0
	 */
	public static <T> JsonElement toJson(final T src, final TypeToken<T> typeToken) {
		return toJson(src, DEFAULT_GSON, typeToken);
	}

	/**
	 * 序列化泛型对象为JsonElement（自定义GSON）
	 *
	 * @param src      要序列化的对象
	 * @param gson     自定义Gson实例
	 * @param typeToken 类型标记
	 * @param <T>      对象类型
	 * @return JsonElement对象（null输入返回JsonNull）
	 * @since 1.0.0
	 */
	public static <T> JsonElement toJson(final T src, final Gson gson, final TypeToken<T> typeToken) {
		if (Objects.isNull(src)) {
			return JsonNull.INSTANCE;
		}
		return gson.toJsonTree(src, typeToken.getType());
	}

	/**
	 * 反序列化JsonArray到List（使用默认GSON）
	 *
	 * @param array JsonArray对象
	 * @param <T>   元素类型
	 * @return List集合（空输入返回空集合）
	 * @since 1.0.0
	 */
	public static <T> List<T> fromJsonArray(final JsonArray array) {
		return fromJsonArray(array, DEFAULT_GSON);
	}

	/**
	 * 反序列化JsonArray到List（自定义GSON）
	 *
	 * @param array JsonArray对象
	 * @param gson  自定义Gson实例
	 * @param <T>   元素类型
	 * @return List集合（空输入返回空集合）
	 * @since 1.0.0
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> fromJsonArray(final JsonArray array, final Gson gson) {
		if (Objects.isNull(array) || array.isEmpty()) {
			return Collections.emptyList();
		}
		return gson.fromJson(array, List.class);
	}

	/**
	 * 反序列化JsonArray到指定类型List（使用默认GSON）
	 *
	 * @param array     JsonArray对象
	 * @param typeToken 列表类型标记
	 * @param <T>       元素类型
	 * @return List集合（空输入返回空集合）
	 * @since 1.0.0
	 */
	public static <T> List<T> fromJsonArray(final JsonArray array, final TypeToken<? extends List<T>> typeToken) {
		return fromJsonArray(array, DEFAULT_GSON, typeToken);
	}

	/**
	 * 反序列化JsonArray到指定类型List（自定义GSON）
	 *
	 * @param array     JsonArray对象
	 * @param gson      自定义Gson实例
	 * @param typeToken 列表类型标记
	 * @param <T>       元素类型
	 * @return List集合（空输入返回空集合）
	 * @since 1.0.0
	 */
	public static <T> List<T> fromJsonArray(final JsonArray array, final Gson gson, final TypeToken<? extends List<T>> typeToken) {
		if (Objects.isNull(array) || array.isEmpty()) {
			return Collections.emptyList();
		}
		return gson.fromJson(array, typeToken.getType());
	}

	/**
	 * 序列化集合为JsonArray（使用默认GSON）
	 *
	 * @param collection 要转换的集合
	 * @param <T>        元素类型
	 * @return JsonArray对象（空输入返回新实例）
	 * @since 1.0.0
	 */
	public static <T> JsonArray toJsonArray(final Collection<T> collection) {
		return toJsonArray(collection, DEFAULT_GSON);
	}

	/**
	 * 序列化集合为JsonArray（自定义GSON）
	 *
	 * @param collection 要转换的集合
	 * @param gson       自定义Gson实例
	 * @param <T>        元素类型
	 * @return JsonArray对象（空输入返回新实例）
	 * @since 1.0.0
	 */
	public static <T> JsonArray toJsonArray(final Collection<T> collection, final Gson gson) {
		if (CollectionUtils.isEmpty(collection)) {
			return new JsonArray();
		}
		JsonElement jsonElement = gson.toJsonTree(collection);
		return jsonElement.getAsJsonArray();
	}

	/**
	 * 序列化集合为JsonArray（指定类型，使用默认GSON）
	 *
	 * @param collection 要转换的集合
	 * @param typeToken  集合类型标记
	 * @param <T>        元素类型
	 * @return JsonArray对象（空输入返回新实例）
	 * @since 1.0.0
	 */
	public static <T> JsonArray toJsonArray(final Collection<T> collection, final TypeToken<Collection<T>> typeToken) {
		return toJsonArray(collection, DEFAULT_GSON, typeToken);
	}

	/**
	 * 序列化集合为JsonArray（指定类型，自定义GSON）
	 *
	 * @param collection 要转换的集合
	 * @param gson       自定义Gson实例
	 * @param typeToken  集合类型标记
	 * @param <T>        元素类型
	 * @return JsonArray对象（空输入返回新实例）
	 * @since 1.0.0
	 */
	public static <T> JsonArray toJsonArray(final Collection<T> collection, final Gson gson, final TypeToken<Collection<T>> typeToken) {
		if (CollectionUtils.isEmpty(collection)) {
			return new JsonArray();
		}
		JsonElement jsonElement = gson.toJsonTree(collection, typeToken.getType());
		return jsonElement.getAsJsonArray();
	}
}