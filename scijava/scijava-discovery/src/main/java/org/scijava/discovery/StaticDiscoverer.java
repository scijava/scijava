
package org.scijava.discovery;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StaticDiscoverer implements Discoverer {

	Map<Object, String> tags;

	public StaticDiscoverer() {
		tags = new HashMap<>();
	}

	public void register(String tag, Object[]... objects) {
		for (Object[] o : objects) {
			register(tag, o);
		}
	}

	public void register(String tag, Object... objects) {
		for (Object o : objects) {
			tags.put(o, tag);
		}
	}

	public void register(String tag, Iterable<?> objects) {
		for (Object o : objects)
			tags.put(o, tag);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> discover(Class<T> c) {
		return tags.keySet().parallelStream() //
				.filter(o -> c.isAssignableFrom(o.getClass())) //
				.map(o -> (T) o) //
				.collect(Collectors.toList());
	}

}
