
package org.scijava.discovery;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Discovers {@link Discovery}s
 * 
 * @author Gabriel Selzer
 */
public interface Discoverer {

	public static Discoverer using(
		Function<Class<?>, ? extends ServiceLoader<?>> func)
	{
		return new Discoverer() {

			@Override
			public <T> List<Discovery<Class<T>>> discoveriesOfType(Class<T> c) {
				// If we can use c, look up the implementations
				try {
					@SuppressWarnings("unchecked")
					ServiceLoader<T> loader = (ServiceLoader<T>) func.apply(c);
					return loader.stream() //
						.map(impl -> {
							String tagType = c.getTypeName().toLowerCase();
							@SuppressWarnings("unchecked")
							Class<T> implClass = (Class<T>) impl.get().getClass();
							return new Discovery<>(implClass, tagType);
						}) //
						.collect(Collectors.toList());
				}
				catch (ServiceConfigurationError e) {
					return Collections.emptyList();
				}
			}

		};
	}

	/**
	 * Discovers implementations of some {@link Class} {@code c}.
	 * 
	 * @param <T> the {@link Type} of the {@link Class} being searched for
	 * @param c the {@link Class} being searched for
	 * @return a {@link List} of implementations of {@code c}, each wrapped up
	 *         into a {@link Discovery}
	 */
	@SuppressWarnings("unused")
	default <T> List<Discovery<Class<T>>> discoveriesOfType(Class<T> c) {
		return Collections.emptyList();
	}

	/**
	 * Discovers implementations of some {@link Class} {@code c}.
	 * 
	 * @param <T> the {@link Type} of the {@link Class} being searched for
	 * @param c the {@link Class} being searched for
	 * @return a {@link List} of implementations of {@code c}
	 */
	default <T> List<Class<T>> implsOfType(Class<T> c) {
		return discoveriesOfType(c).parallelStream() //
				.map(d -> d.discovery()) //
				.collect(Collectors.toList());
	}

	/**
	 * Discovers {@link AnnotatedElement}s tagged with the tag {@code tag}. 
	 * @param tag the tag type of interest
	 * @return a {@link List} of {@link AnnotatedElement}s 
	 */
	
	@SuppressWarnings("unused")
	default List<Discovery<AnnotatedElement>> elementsTaggedWith(String tag) {
		return Collections.emptyList();
	}

}
