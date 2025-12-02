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
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Gson 类型适配器：在 JSON 与 {@link BigDecimal} 之间转换。
 * <p>
 * 序列化：输出为十进制字符串（使用 {@link BigDecimal#toPlainString()}，避免科学计数法）。<br>
 * 反序列化：支持的输入 token：
 * <ul>
 *   <li>{@link com.google.gson.stream.JsonToken#NULL}：返回 {@code null}</li>
 *   <li>{@link com.google.gson.stream.JsonToken#STRING}：使用 {@link BigDecimal#BigDecimal(String)} 解析，格式非法时返回 {@code null}</li>
 *   <li>{@link com.google.gson.stream.JsonToken#NUMBER}：读取为长整型并转换为 {@link BigDecimal}（{@link BigDecimal#valueOf(long)})</li>
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
 *     .registerTypeAdapter(BigDecimal.class, new BigDecimalTypeAdapter())
 *     .create();
 * }</pre>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class BigDecimalTypeAdapter extends TypeAdapter<BigDecimal> {
	/**
	 * 将 {@link BigDecimal} 写出为 JSON。
	 * <p>输出为十进制字符串（不使用科学记数法）；当值为 {@code null} 时输出 JSON {@code null}。</p>
	 *
	 * @param out   JSON 写出器（由 Gson 管理其生命周期）
	 * @param value 待写出的 {@link BigDecimal} 值
	 * @throws IOException 写出失败时抛出
	 */
	@Override
	public void write(JsonWriter out, BigDecimal value) throws IOException {
		if (Objects.isNull(value)) {
			out.nullValue();
		} else {
			out.value(value.toPlainString());
		}
	}

	/**
	 * 从 JSON 读取为 {@link BigDecimal}。
	 * <p>
	 * 支持：
	 * <ul>
	 *   <li>NULL → {@code null}</li>
	 *   <li>STRING → {@link BigDecimal}（字符串格式非法时返回 {@code null}）</li>
	 *   <li>NUMBER（整数或小数） → {@link BigDecimal#valueOf(double)}</li>
	 * </ul>
	 * 其他 token 类型抛出 {@link JsonParseException}。
	 * </p>
	 * <p>
	 * 注意：当前实现通过 {@link com.google.gson.stream.JsonReader#nextDouble()} 读取数字，
	 * 再调用 {@link BigDecimal#valueOf(double)} 转换，可能存在二进制浮点与十进制精度差异；
	 * 若需严格十进制精度，建议改为读取字符串并使用 {@code new BigDecimal(String)} 构造。
	 * </p>
	 *
	 * @param in JSON 读取器（由 Gson 管理其生命周期）
	 * @return 解析得到的 {@link BigDecimal} 或 {@code null}
	 * @throws IOException 读取失败时抛出
	 * @throws JsonParseException 当 token 类型不是数字、字符串或 NULL 时抛出
	 */
	@Override
	public BigDecimal read(JsonReader in) throws IOException {
		JsonToken token = in.peek();
		if (token == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		if (token == JsonToken.STRING) {
			try {
				return new BigDecimal(in.nextString());
			} catch (NumberFormatException e) {
				return null;
			}
		}
		if (token == JsonToken.NUMBER) {
			return BigDecimal.valueOf(in.nextDouble());
		}
		throw new JsonParseException("预期为数字、字符串或 NULL，但结果为" + token);
	}
}