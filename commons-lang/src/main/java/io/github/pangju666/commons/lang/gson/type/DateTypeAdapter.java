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

package io.github.pangju666.commons.lang.gson.type;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.github.pangju666.commons.lang.utils.DateUtils;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

/**
 * Gson 类型适配器：在 JSON 与 {@link Date} 之间转换。
 * <p>
 * 序列化：输出为自 1970-01-01T00:00:00Z 起的毫秒数（JSON 数字）。<br>
 * 反序列化：支持的输入 token：
 * <ul>
 *   <li>{@link com.google.gson.stream.JsonToken#NULL}：返回 {@code null}</li>
 *   <li>{@link com.google.gson.stream.JsonToken#NUMBER}：将毫秒数转换为 {@link Date}</li>
 *   <li>{@link com.google.gson.stream.JsonToken#STRING}：使用 {@link io.github.pangju666.commons.lang.utils.DateUtils#parseDate(String)} 解析为 {@link Date}</li>
 *   <li>其他类型：抛出 {@link com.google.gson.JsonParseException}</li>
 * </ul>
 * </p>
 *
 * <h3>线程安全</h3>
 * <p>类无状态，可在多线程中安全复用。</p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * Gson gson = new GsonBuilder()
 *     .registerTypeAdapter(Date.class, new DateTypeAdapter())
 *     .create();
 * }</pre>
 *
 * @author pangju666
 * @see io.github.pangju666.commons.lang.utils.DateUtils#parseDate(String)
 * @since 1.0.0
 */
public class DateTypeAdapter extends TypeAdapter<Date> {
	/**
	 * 将 {@link Date} 写出为 JSON。
	 * <p>输出为毫秒时间戳（JSON 数字）；当值为 {@code null} 时输出 JSON {@code null}。</p>
	 *
	 * @param out   JSON 写出器（由 Gson 管理其生命周期）
	 * @param value 待写出的 {@link Date} 值
	 * @throws IOException 写出失败时抛出
	 */
	@Override
	public void write(JsonWriter out, Date value) throws IOException {
		if (Objects.isNull(value)) {
			out.nullValue();
		} else {
			out.value(value.getTime());
		}
	}

	/**
	 * 从 JSON 读取为 {@link Date}。
	 * <p>
	 * 支持：
	 * <ul>
	 *   <li>NULL → {@code null}</li>
	 *   <li>NUMBER（毫秒时间戳） → {@link Date}</li>
	 *   <li>STRING（由 {@link io.github.pangju666.commons.lang.utils.DateUtils#parseDate(String)} 支持的日期格式） → {@link Date}</li>
	 * </ul>
	 * 其他 token 类型抛出 {@link JsonParseException}。
	 * </p>
	 *
	 * @param in JSON 读取器（由 Gson 管理其生命周期）
	 * @return 解析得到的 {@link Date} 或 {@code null}
	 * @throws IOException        读取失败时抛出
	 * @throws JsonParseException 当 token 类型不是数字、字符串或 NULL 时抛出
	 */
	@Override
	public Date read(JsonReader in) throws IOException {
		JsonToken token = in.peek();
		if (token == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		if (token == JsonToken.STRING) {
			return DateUtils.parseDate(in.nextString());
		}
		if (token == JsonToken.NUMBER) {
			return new Date(in.nextLong());
		}
		throw new JsonParseException("预期为数字、字符串或 NULL，但结果为" + token);
	}
}