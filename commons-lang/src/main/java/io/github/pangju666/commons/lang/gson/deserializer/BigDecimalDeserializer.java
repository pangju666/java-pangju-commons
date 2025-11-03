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

import java.lang.reflect.Type;
import java.math.BigDecimal;

/**
 * BigDecimal 类型的 Gson 自定义反序列化器
 * <p>
 * 该反序列化器用于将 JSON 元素转换为 BigDecimal 对象。支持从以下类型的 JSON 元素进行转换：
 * <ul>
 *     <li>数字类型：使用 {@link BigDecimal#valueOf(long)} 进行转换</li>
 *     <li>字符串类型：使用 {@link BigDecimal#BigDecimal(String)} 构造函数进行转换</li>
 * </ul>
 * 对于非基本类型的 JSON 元素或无法转换的情况，将返回 null。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * GsonBuilder gsonBuilder = new GsonBuilder();
 * gsonBuilder.registerTypeAdapter(BigDecimal.class, new BigDecimalDeserializer());
 * Gson gson = gsonBuilder.create();
 * }</pre>
 * </p>
 *
 * @author pangju666
 * @see JsonDeserializer
 * @see BigDecimal
 * @since 1.0.0
 */
public class BigDecimalDeserializer implements JsonDeserializer<BigDecimal> {
	/**
	 * 将 JSON 元素反序列化为 BigDecimal 对象
	 * <p>
	 * 反序列化逻辑：
	 * <ol>
	 *     <li>检查 JSON 元素是否为基本类型（{@link JsonPrimitive}），如果不是则返回 null</li>
	 *     <li>如果是数字类型，则使用 {@link BigDecimal#valueOf(long)} 进行转换</li>
	 *     <li>如果是字符串类型，则尝试使用 {@link BigDecimal#BigDecimal(String)} 构造函数进行转换</li>
	 *     <li>如果字符串无法转换为有效的 BigDecimal（抛出 {@link NumberFormatException}），则返回 null</li>
	 *     <li>对于其他类型（如布尔值），返回 null</li>
	 * </ol>
	 * </p>
	 *
	 * @param json    要反序列化的 JSON 元素
	 * @param typeOfT 目标类型，在此实现中未使用
	 * @param context 反序列化上下文，在此实现中未使用
	 * @return 转换后的 BigDecimal 对象，如果无法转换则返回 null
	 * @throws JsonParseException 如果在解析过程中发生错误
	 */
	@Override
	public BigDecimal deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if (!json.isJsonPrimitive()) {
			return null;
		}
		JsonPrimitive primitive = json.getAsJsonPrimitive();
		if (primitive.isNumber()) {
			return BigDecimal.valueOf(primitive.getAsDouble());
		} else if (json.getAsJsonPrimitive().isString()) {
			try {
				return new BigDecimal(primitive.getAsString());
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}
}