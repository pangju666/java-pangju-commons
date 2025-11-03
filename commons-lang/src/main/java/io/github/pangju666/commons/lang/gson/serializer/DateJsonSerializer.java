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

import java.lang.reflect.Type;
import java.util.Date;

/**
 * Date 类型的 Gson 自定义序列化器
 * <p>
 * 该序列化器用于将 Date 对象转换为 JSON 数值元素。
 * 序列化时会将 Date 对象转换为自 1970 年 1 月 1 日 00:00:00 GMT 以来的毫秒数。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * GsonBuilder gsonBuilder = new GsonBuilder();
 * gsonBuilder.registerTypeAdapter(Date.class, new DateJsonSerializer());
 * Gson gson = gsonBuilder.create();
 *
 * // 序列化示例
 * Date date = new Date(); // 当前时间
 * String json = gson.toJson(date); // 输出类似: "1640995200000"
 * }</pre>
 * </p>
 *
 * @author pangju666
 * @see JsonSerializer
 * @see Date
 * @see Date#getTime()
 * @since 1.0.0
 */
public class DateJsonSerializer implements JsonSerializer<Date> {
	/**
	 * 将 Date 对象序列化为 JSON 元素
	 * <p>
	 * 序列化逻辑：
	 * <ol>
	 *     <li>使用 {@link Date#getTime()} 获取时间戳（毫秒数）</li>
	 *     <li>将时间戳转换为 {@link JsonPrimitive} 数值元素</li>
	 * </ol>
	 * </p>
	 *
	 * @param src       要序列化的 Date 对象
	 * @param typeOfSrc 源类型，在此实现中未使用
	 * @param context   序列化上下文，在此实现中未使用
	 * @return 包含时间戳的 JSON 数值元素
	 */
	@Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getTime());
    }
}
