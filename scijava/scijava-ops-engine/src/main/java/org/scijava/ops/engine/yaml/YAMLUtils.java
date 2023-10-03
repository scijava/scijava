package org.scijava.ops.engine.yaml;

import java.util.HashMap;
import java.util.Map;

public class YAMLUtils {


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

	public static <T> T value(final Map<String, Object> map, String key, Class<T> type) {
		if (!map.containsKey(key)) {
			throw new IllegalArgumentException("YAML map " + map +
					" does not contain key " + key);
		}
		return (T) map.get(key);
	}

	public static String value(final Map<String, Object> map, String key, String defaultValue) {
		return map.getOrDefault(key, defaultValue).toString().trim();
	}

}
