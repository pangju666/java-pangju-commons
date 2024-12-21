package io.github.pangju666.commons.lang.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class JsonUtils {
	public static final Gson DEFAULT_GSON;

	static {
		DEFAULT_GSON = new GsonBuilder()
			.setPrettyPrinting()
			.create();
	}

	protected JsonUtils() {
	}

	public static JsonElement parseString(final String json) {
		if (StringUtils.isBlank(json)) {
			return JsonNull.INSTANCE;
		}
		return JsonParser.parseString(json);
	}

	public static <T> T fromString(final String json) {
		return fromString(json, DEFAULT_GSON, new TypeToken<T>() {
		});
	}

	public static <T> T fromString(final String json, final TypeToken<T> typeToken) {
		return fromString(json, DEFAULT_GSON, typeToken);
	}

	public static <T> T fromString(final String json, final Gson gson) {
		return fromString(json, gson, new TypeToken<T>() {
		});
	}

	public static <T> T fromString(final String json, final Gson gson, final TypeToken<T> typeToken) {
		if (StringUtils.isBlank(json)) {
			return null;
		}
		return gson.fromJson(json, typeToken.getType());
	}

	public static <T> String toString(final T src) {
		return toString(src, DEFAULT_GSON, new TypeToken<T>() {
		});
	}

	public static <T> String toString(final T src, final TypeToken<T> typeToken) {
		return toString(src, DEFAULT_GSON, typeToken);
	}

	public static <T> String toString(final T src, final Gson gson) {
		return toString(src, gson, new TypeToken<T>() {
		});
	}

	public static <T> String toString(final T src, final Gson gson, final TypeToken<T> typeToken) {
		if (Objects.isNull(src)) {
			return StringUtils.EMPTY;
		}
		return gson.toJson(src, typeToken.getType());
	}

	public static <T> T fromJson(final JsonElement json) {
		return fromJson(json, DEFAULT_GSON, new TypeToken<T>() {
		});
	}

	public static <T> T fromJson(final JsonElement json, final TypeToken<T> typeToken) {
		return fromJson(json, DEFAULT_GSON, typeToken);
	}

	public static <T> T fromJson(final JsonElement json, final Gson gson) {
		return fromJson(json, gson, new TypeToken<T>() {
		});
	}

	public static <T> T fromJson(final JsonElement json, final Gson gson, final TypeToken<T> typeToken) {
		return gson.fromJson(json, typeToken.getType());
	}

	public static <T> JsonElement toJson(final T src) {
		return toJson(src, DEFAULT_GSON, new TypeToken<T>() {
		});
	}

	public static <T> JsonElement toJson(final T src, final TypeToken<T> typeToken) {
		return toJson(src, DEFAULT_GSON, typeToken);
	}

	public static <T> JsonElement toJson(final T src, final Gson gson) {
		return toJson(src, gson, new TypeToken<T>() {
		});
	}

	public static <T> JsonElement toJson(final T src, final Gson gson, final TypeToken<T> typeToken) {
		if (Objects.isNull(src)) {
			return JsonNull.INSTANCE;
		}
		return gson.toJsonTree(src, typeToken.getType());
	}

	public static <T> List<T> fromJsonArray(final JsonArray array) {
		return fromJsonArray(array, DEFAULT_GSON, new TypeToken<List<T>>() {
		});
	}

	public static <T> List<T> fromJsonArray(final JsonArray array, final TypeToken<? extends List<T>> typeToken) {
		return fromJsonArray(array, DEFAULT_GSON, typeToken);
	}

	public static <T> List<T> fromJsonArray(final JsonArray json, final Gson gson) {
		return fromJsonArray(json, gson, new TypeToken<List<T>>() {
		});
	}

	public static <T> List<T> fromJsonArray(final JsonArray json, final Gson gson, final TypeToken<? extends List<T>> typeToken) {
		if (json == null || json.isEmpty()) {
			return Collections.emptyList();
		}
		return gson.fromJson(json, typeToken.getType());
	}

	public static <T> JsonArray toJsonArray(final Collection<T> collection) {
		return toJsonArray(collection, DEFAULT_GSON, new TypeToken<Collection<T>>() {
		});
	}

	public static <T> JsonArray toJsonArray(final Collection<T> collection, final Gson gson) {
		return toJsonArray(collection, gson, new TypeToken<Collection<T>>() {
		});
	}

	public static <T> JsonArray toJsonArray(final Collection<T> collection, final Gson gson, final TypeToken<Collection<T>> typeToken) {
		if (CollectionUtils.isEmpty(collection)) {
			return new JsonArray();
		}
		JsonElement jsonElement = gson.toJsonTree(collection, typeToken.getType());
		return jsonElement.getAsJsonArray();
	}
}