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

/**
 * Class 类型的 Gson 自定义序列化器
 * <p>
 * 该序列化器用于将 Class 对象转换为 JSON 字符串元素。
 * 序列化时会将 Class 对象转换为其完全限定类名（包含包名的类名）。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * GsonBuilder gsonBuilder = new GsonBuilder();
 * gsonBuilder.registerTypeAdapter(Class.class, new ClassJsonSerializer());
 * Gson gson = gsonBuilder.create();
 *
 * // 序列化示例
 * String json = gson.toJson(String.class); // 输出: "java.lang.String"
 * }</pre>
 * </p>
 *
 * @author pangju666
 * @see JsonSerializer
 * @see Class
 * @see Class#getName()
 * @since 1.0.0
 */
public class ClassJsonSerializer implements JsonSerializer<Class> {
	/**
	 * 将 Class 对象序列化为 JSON 元素
	 * <p>
	 * 序列化逻辑：
	 * <ol>
	 *     <li>使用 {@link Class#getName()} 获取类的完全限定名</li>
	 *     <li>将类名转换为 {@link JsonPrimitive} 字符串元素</li>
	 * </ol>
	 * </p>
	 *
	 * @param src       要序列化的 Class 对象
	 * @param typeOfSrc 源类型，在此实现中未使用
	 * @param context   序列化上下文，在此实现中未使用
	 * @return 包含类名的 JSON 字符串元素
	 */
	@Override
	public JsonElement serialize(Class src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(src.getName());
	}
}
