
package org.scijava.types;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.scijava.discovery.Discoverer;
import org.scijava.log2.Logger;

/**
 * TODO: Hide implementation
 * 
 * @author Gabriel Selzer
 * @author Curtis Rueden
 */
public class DefaultTypeReifier implements TypeReifier {

	private Map<Class<?>, TypeExtractor<?>> extractors;
	private final Logger log;
	private final Collection<Discoverer> discoverers;

	public DefaultTypeReifier(Logger log, Discoverer... discoverers) {
		this(log, Arrays.asList(discoverers));
	}

	public DefaultTypeReifier(Logger log, Collection<Discoverer> discoverers) {
		this.log = log;
		this.discoverers = discoverers;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> TypeExtractor<T> getExtractor(final Class<T> c) {
		return (TypeExtractor<T>) extractors().get(c);
	}

	@Override
	public Logger log() {
		return log;
	}

	// -- Helper methods --

	private Map<Class<?>, TypeExtractor<?>> extractors() {
		if (extractors == null) initExtractors();
		return extractors;
	}

	private synchronized void initExtractors() {
		if (extractors != null) return;

		final HashMap<Class<?>, TypeExtractor<?>> map = new HashMap<>();

		for (final TypeExtractor<?> typeExtractor : getInstances()) {
			final Class<?> key = typeExtractor.getRawType();
			if (!map.containsKey(key)) map.put(key, typeExtractor);
		}

		extractors = map;
	}

	private List<TypeExtractor<?>> getInstances() {
		return discoverers.parallelStream().map(d -> {
			return d.implsOfType(TypeExtractor.class) //
				.parallelStream() //
				.map(cls -> getInstance(cls)) //
				.collect(Collectors.toList());
		}).flatMap(l -> l.parallelStream()).filter(Objects::nonNull).collect(
			Collectors.toList());
	}

	private TypeExtractor<?> getInstance(
		@SuppressWarnings("rawtypes") Class<TypeExtractor> c)
	{
		try {
			return c.getDeclaredConstructor().newInstance();
		}
		catch (Exception exc) {
			log().warn(exc);
			return null;
		}
	}

}
