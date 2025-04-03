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
import java.math.BigInteger;

public class BigIntegerDeserializer implements JsonDeserializer<BigInteger> {
	@Override
	public BigInteger deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if (!json.isJsonPrimitive()) {
			return null;
		}
		JsonPrimitive primitive = json.getAsJsonPrimitive();
		if (primitive.isNumber()) {
			return BigInteger.valueOf(primitive.getAsLong());
		} else if (primitive.isString()) {
			try {
				return new BigInteger(primitive.getAsString());
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}
}