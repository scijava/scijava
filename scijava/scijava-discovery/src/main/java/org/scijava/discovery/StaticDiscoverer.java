
package org.scijava.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StaticDiscoverer implements Discoverer {

	Map<Class<?>, String> tags;

	public StaticDiscoverer() {
		tags = new HashMap<>();
	}

	public void register(Class<?> c, String tag) {
		tags.put(c, tag);
	}

	public void register(Class<?> c, String tagType, String tagData) {
		tags.put(c, String.join(" ", tagType, tagData));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<Discovery<Class<T>>> discoveriesOfType(Class<T> c) {
		return tags.keySet().stream() //
			.filter(cls -> cls.isAssignableFrom(c)) //
			.map(cls -> new Discovery<>((Class<T>) cls, tags.get(cls))) //
			.collect(Collectors.toList());
	}

}
