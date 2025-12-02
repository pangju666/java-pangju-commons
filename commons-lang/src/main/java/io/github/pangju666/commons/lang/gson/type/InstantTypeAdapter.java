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

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

/**
 * Gson 类型适配器：在 JSON 与 {@link Instant} 之间转换。
 * <p>
 * 序列化：输出为自 1970-01-01T00:00:00Z 起的毫秒数（JSON 数字）。<br>
 * 反序列化：支持的输入 token：
 * <ul>
 *   <li>{@link JsonToken#NULL}：返回 {@code null}</li>
 *   <li>{@link JsonToken#NUMBER}：将毫秒数转换为 {@link Instant}</li>
 *   <li>其他类型：抛出 {@link JsonParseException}</li>
 * </ul>
 * </p>
 *
 * <h3>线程安全</h3>
 * <p>类无状态，可在多线程中安全复用。</p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * Gson gson = new GsonBuilder()
 *     .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
 *     .create();
 * }</pre>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class InstantTypeAdapter extends TypeAdapter<Instant> {
	/**
	 * 将 {@link Instant} 写出为 JSON。
	 * <p>输出为毫秒时间戳（JSON 数字）；当值为 {@code null} 时输出 JSON {@code null}。</p>
	 *
	 * @param out   JSON 写出器（由 Gson 管理其生命周期）
	 * @param value 待写出的 {@link Instant} 值
	 * @throws IOException 写出失败时抛出
	 */
	@Override
	public void write(JsonWriter out, Instant value) throws IOException {
		if (Objects.isNull(value)) {
			out.nullValue();
		} else {
			out.value(value.toEpochMilli());
		}
	}

	/**
	 * 从 JSON 读取为 {@link Instant}。
	 * <p>支持 {@code NULL}→{@code null}，以及数字毫秒→{@link Instant}；其他 token 类型抛出 {@link JsonParseException}。</p>
	 *
	 * @param in JSON 读取器（由 Gson 管理其生命周期）
	 * @return 解析得到的 {@link Instant} 或 {@code null}
	 * @throws IOException        读取失败时抛出
	 * @throws JsonParseException 当 token 类型不是数字或 NULL 时抛出
	 */
	@Override
	public Instant read(JsonReader in) throws IOException {
		JsonToken token = in.peek();
		if (token == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		if (token == JsonToken.NUMBER) {
			return Instant.ofEpochMilli(in.nextLong());
		}
		throw new JsonParseException("预期为数字或 NULL，但结果为" + token);
	}
}