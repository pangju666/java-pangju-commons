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

package io.github.pangju666.commons.lang.gson.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.pangju666.commons.lang.utils.DateUtils;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

/**
 * LocalDateTime 类型的 Gson 自定义序列化器
 * <p>
 * 该序列化器用于将 LocalDateTime 对象转换为 JSON 数值元素。
 * 序列化过程会先将 LocalDateTime 转换为 Date 对象，然后获取自 1970 年 1 月 1 日 00:00:00 GMT 以来的毫秒数。
 * 转换时会使用系统默认时区。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * GsonBuilder gsonBuilder = new GsonBuilder();
 * gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeJsonSerializer());
 * Gson gson = gsonBuilder.create();
 *
 * // 序列化示例
 * LocalDateTime dateTime = LocalDateTime.of(2022, 1, 1, 12, 30, 0);
 * String json = gson.toJson(dateTime); // 输出类似: "1641040200000"
 * }</pre>
 * </p>
 *
 * @author pangju666
 * @see JsonSerializer
 * @see LocalDateTime
 * @see DateUtils#toDate(LocalDateTime)
 * @since 1.0.0
 */
public class LocalDateTimeJsonSerializer implements JsonSerializer<LocalDateTime> {
	/**
	 * 将 LocalDateTime 对象序列化为 JSON 元素
	 * <p>
	 * 序列化逻辑：
	 * <ol>
	 *     <li>使用 {@link DateUtils#toDate(LocalDateTime)} 将 LocalDateTime 转换为 Date 对象</li>
	 *     <li>调用 {@link java.util.Date#getTime()} 获取时间戳（毫秒数）</li>
	 *     <li>将时间戳转换为 {@link JsonPrimitive} 数值元素</li>
	 * </ol>
	 * </p>
	 *
	 * @param src       要序列化的 LocalDateTime 对象
	 * @param typeOfSrc 源类型，在此实现中未使用
	 * @param context   序列化上下文，在此实现中未使用
	 * @return 包含时间戳的 JSON 数值元素
	 */
	@Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(DateUtils.toDate(src).getTime());
    }
}
