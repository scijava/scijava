/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ops.engine.yaml;

import java.util.HashMap;
import java.util.Map;

public final class YAMLUtils {

	private YAMLUtils() {
		// Prevent instantiation of static utility class
	}

	public static Map<String, Object> subMap(final Map<String, Object> map,
		String key)
	{
		if (!map.containsKey(key)) {
			throw new IllegalArgumentException("YAML map " + map +
				" does not contain key " + key);
		}
		Object value = map.get(key);
		if (!(value instanceof Map)) {
			throw new IllegalArgumentException("YAML map " + map +
				" has a non-map value for key " + key);
		}
		return (Map<String, Object>) value;
	}

	public static String value(final Map<String, Object> map, String key) {
		if (!map.containsKey(key)) {
			throw new IllegalArgumentException("YAML map " + map +
				" does not contain key " + key);
		}
		return map.get(key).toString().trim();
	}

	public static <T> T value(final Map<String, Object> map, String key,
		Class<T> type)
	{
		if (!map.containsKey(key)) {
			throw new IllegalArgumentException("YAML map " + map +
				" does not contain key " + key);
		}
		return (T) map.get(key);
	}

	public static String value(final Map<String, Object> map, String key,
		String defaultValue)
	{
		return map.getOrDefault(key, defaultValue).toString().trim();
	}

}
