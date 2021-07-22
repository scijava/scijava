
package org.scijava.ops.discovery;

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
	public <T> List<? extends Class<T>> implementingClasses(Class<T> c) {
		return names.keySet().stream() //
			.filter(cls -> cls.isAssignableFrom(c)) //
			.map(cls -> (Class<T>) cls) //
			.collect(Collectors.toList());
	}

	@Override
	public <T> List<? extends T> implementingInstances(Class<T> c,
		Class<?>[] constructorClasses, Object[] constructorArgs)
	{
		return implementingClasses(c).stream() //
			.map(cls -> classToObjectOrNull(cls, constructorClasses, constructorArgs)) //
			.filter(o -> o != null) //
			.collect(Collectors.toList());
	}

	private <T> T classToObjectOrNull(Class<T> c, Class<?>[] constructorClasses, Object[] constructorArgs) {
			try {
				return c.getDeclaredConstructor(constructorClasses).newInstance(
					constructorArgs);
			}
			catch (Throwable t)
			{
				return null;
			}
	}

	@Override
	public <T> List<Implementation<T>> implementationsOf(Class<T> c) {
		return implementingClasses(c).stream() //
				.map(cls -> new Implementation<>(cls, c, names.get(cls))) //
				.collect(Collectors.toList());
	}

}
