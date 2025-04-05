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

package io.github.pangju666.commons.lang.gson.deserializer;

import com.google.gson.*;
import io.github.pangju666.commons.lang.utils.DateUtils;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

/**
 * LocalDateTime 类型的 Gson 自定义反序列化器
 * <p>
 * 该反序列化器用于将 JSON 数值元素（表示时间戳）转换为 LocalDateTime 对象。
 * 仅支持从数值类型的时间戳进行转换，时间戳应为自 1970 年 1 月 1 日 00:00:00 GMT 以来的毫秒数。
 * 转换时会根据系统默认时区进行处理。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * GsonBuilder gsonBuilder = new GsonBuilder();
 * gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeJsonDeserializer());
 * Gson gson = gsonBuilder.create();
 *
 * // 反序列化示例
 * LocalDateTime dateTime = gson.fromJson("1640995200000", LocalDateTime.class); // 2022-01-01 00:00:00
 * }</pre>
 * </p>
 *
 * @author pangju666
 * @see JsonDeserializer
 * @see LocalDateTime
 * @see DateUtils#toLocalDateTime(Long)
 * @since 1.0.0
 */
public class LocalDateTimeJsonDeserializer implements JsonDeserializer<LocalDateTime> {
	/**
	 * 将 JSON 元素反序列化为 LocalDateTime 对象
	 * <p>
	 * 反序列化逻辑：
	 * <ol>
	 *     <li>检查 JSON 元素是否为数值类型的基本类型（{@link JsonPrimitive}），如果不是则返回 null</li>
	 *     <li>使用 {@link DateUtils#toLocalDateTime(Long)} 将时间戳转换为 {@link LocalDateTime} 对象</li>
	 * </ol>
	 * </p>
	 *
	 * @param json    要反序列化的 JSON 元素，应该是表示时间戳的数值
	 * @param typeOfT 目标类型，在此实现中未使用
	 * @param context 反序列化上下文，在此实现中未使用
	 * @return 转换后的 LocalDateTime 对象，如果输入不是数值类型则返回 null
	 * @throws JsonParseException 如果在解析过程中发生错误
	 */
	@Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isNumber()) {
            return null;
        }
		if (json.getAsJsonPrimitive().isNumber()) {
			return DateUtils.toLocalDateTime(json.getAsLong());
		}
		if (json.getAsJsonPrimitive().isString()) {
			return DateUtils.toLocalDateTime(DateUtils.parseDate(json.getAsString()));
		}
		return null;
    }
}