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
import io.github.pangju666.commons.lang.pool.Constants;
import io.github.pangju666.commons.lang.utils.DateUtils;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * LocalDate 类型的 Gson 自定义反序列化器
 * <p>
 * 该反序列化器支持以下两种格式的日期时间反序列化：
 * <ul>
 *     <li>时间戳：将JSON数值元素（毫秒时间戳）转换为LocalDate对象</li>
 *     <li>日期时间字符串：将符合{@link Constants#DATE_FORMAT}, {@link Constants#DATETIME_FORMAT}格式的字符串转换为LocalDate对象</li>
 * </ul>
 * 转换时会根据系统默认时区进行处理。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * GsonBuilder gsonBuilder = new GsonBuilder();
 * gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateJsonDeserializer());
 * Gson gson = gsonBuilder.create();
 *
 * // 时间戳反序列化示例
 * LocalDate date1 = gson.fromJson("1640995200000", LocalDate.class); // 2022-01-01
 * // 日期时间字符串反序列化示例
 * LocalDate date2 = gson.fromJson("2022-01-01", LocalDate.class); // 2022-01-01
 * }</pre>
 * </p>
 *
 * @author pangju666
 * @see JsonDeserializer
 * @see LocalDateTime
 * @see DateUtils#toLocalDate(Long)
 * @see DateUtils#toLocalDate(Date)
 * @since 1.0.0
 */
public class LocalDateJsonDeserializer implements JsonDeserializer<LocalDate> {
	/**
	 * 将 JSON 元素反序列化为 LocalDate 对象
	 * <p>
	 * 反序列化逻辑：
	 * <ol>
	 *     <li>检查 JSON 元素是否为基本类型（{@link JsonPrimitive}），如果不是则返回 null</li>
	 *     <li>如果是数值类型，使用 {@link DateUtils#toLocalDate(Long)} 将时间戳转换为 {@link LocalDate} 对象</li>
	 *     <li>如果是字符串类型，先使用 {@link DateUtils#parseDate(String)} 解析为Date，再通过 {@link DateUtils#toLocalDate(Date)} 转换为LocalDate对象</li>
	 * </ol>
	 * </p>
	 *
	 * @param json    要反序列化的 JSON 元素，可以是时间戳数值或日期时间格式字符串
	 * @param typeOfT 目标类型，在此实现中未使用
	 * @param context 反序列化上下文，在此实现中未使用
	 * @return 转换后的 LocalDate 对象，如果输入格式不支持则返回 null
	 * @throws JsonParseException 如果在解析过程中发生错误
	 */
	@Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if (!json.isJsonPrimitive()) {
            return null;
        }
		if (json.getAsJsonPrimitive().isNumber()) {
			return DateUtils.toLocalDate(json.getAsLong());
		}
		if (json.getAsJsonPrimitive().isString()) {
			return DateUtils.toLocalDate(DateUtils.parseDate(json.getAsString()));
		}
		return null;
    }
}