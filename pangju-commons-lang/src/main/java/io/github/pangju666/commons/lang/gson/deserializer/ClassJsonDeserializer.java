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

/**
 * Class 类型的 Gson 自定义反序列化器
 * <p>
 * 该反序列化器用于将 JSON 字符串元素转换为 Class 对象。
 * JSON 字符串必须是一个有效的类全限定名（例如："java.lang.String"）。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * GsonBuilder gsonBuilder = new GsonBuilder();
 * gsonBuilder.registerTypeAdapter(Class.class, new ClassJsonDeserializer());
 * Gson gson = gsonBuilder.create();
 *
 * // 反序列化示例
 * Class<?> clazz = gson.fromJson("\"java.lang.String\"", Class.class);
 * // 结果: class java.lang.String
 * }</pre>
 * </p>
 *
 * @author pangju666
 * @see JsonDeserializer
 * @see Class
 * @since 1.0.0
 */
public class ClassJsonDeserializer implements JsonDeserializer<Class> {
	/**
	 * 将 JSON 元素反序列化为 Class 对象
	 * <p>
	 * 反序列化逻辑：
	 * <ol>
	 *     <li>检查 JSON 元素是否为字符串类型的基本类型（{@link JsonPrimitive}），如果不是则返回 null</li>
	 *     <li>尝试使用 {@link Class#forName(String)} 加载类，将字符串转换为对应的 Class 对象</li>
	 *     <li>如果类不存在（抛出 {@link ClassNotFoundException}），则返回 null</li>
	 * </ol>
	 * </p>
	 *
	 * @param json    要反序列化的 JSON 元素，应该是包含类全限定名的字符串
	 * @param typeOfT 目标类型，在此实现中未使用
	 * @param context 反序列化上下文，在此实现中未使用
	 * @return 对应的 Class 对象，如果无法转换则返回 null
	 * @throws JsonParseException 如果在解析过程中发生错误
	 */
	@Override
	public Class deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isString()) {
			return null;
		}
		try {
			return Class.forName(json.getAsString());
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
}