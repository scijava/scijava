
package org.scijava.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StaticDiscoverer implements Discoverer {

	Map<Class<?>, String> names;

	public StaticDiscoverer() {
		names = new HashMap<>();
	}

	public void register(Class<?> c, String name) {
		names.put(c, name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<Class<T>> implsOfType(Class<T> c) {
		return names.keySet().stream() //
			.filter(cls -> cls.isAssignableFrom(c)) //
			.map(cls -> (Class<T>) cls) //
			.collect(Collectors.toList());
	}

}
