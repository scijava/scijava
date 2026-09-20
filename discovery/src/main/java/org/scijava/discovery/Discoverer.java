/*-
 * #%L
 * Plugin discovery subsystem for SciJava libraries.
 * %%
 * Copyright (C) 2021 - 2025 SciJava developers.
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

package org.scijava.discovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A source of {@link Discovery discoveries}: implementations of some type,
 * found by some mechanism - {@link ServiceLoader}, a generated annotation
 * index, a YAML file, or a hand-curated list.
 * <p>
 * {@link #discover} deliberately yields <em>descriptors</em> rather than
 * constructed objects, so that mechanisms able to report what is available
 * without loading classes can do so. Callers that simply want the objects can
 * use {@link #instances}.
 * </p>
 *
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
@FunctionalInterface
public interface Discoverer {

	/**
	 * Discovers implementations of the given type.
	 *
	 * @param c the type of interest
	 * @return descriptors of the implementations found
	 */
	<U> List<Discovery<U>> discover(Class<U> c);

	/**
	 * Discovers implementations of the given type, constructing each one.
	 *
	 * @param c the type of interest
	 * @return the implementations found
	 */
	default <U> List<U> instances(final Class<U> c) {
		return discover(c).stream() //
			.map(Discovery::get) //
			.collect(Collectors.toList());
	}

	/**
	 * Creates a lazy {@code Discoverer} from a function yielding
	 * {@link ServiceLoader.Provider}s, which report each implementation's class
	 * without constructing it.
	 * <p>
	 * Callers pass {@code c -> ServiceLoader.load(c).stream()}. The function
	 * must be written in the <em>calling</em> module, because
	 * {@link ServiceLoader} resolves {@code uses} declarations against the
	 * module of its caller; a generic discovery module cannot perform the
	 * lookup on another module's behalf.
	 * </p>
	 *
	 * @param func yields the providers of a given type
	 * @return a lazy discoverer wrapping that function
	 */
	static <T> Discoverer usingProviders(
		final Function<Class<T>, Stream<ServiceLoader.Provider<T>>> func)
	{
		return new Discoverer() {

			@Override
			@SuppressWarnings("unchecked")
			public <U> List<Discovery<U>> discover(final Class<U> c) {
				try {
					return ((Stream<ServiceLoader.Provider<U>>) (Stream<?>) //
					func.apply((Class<T>) c)) //
						.map(p -> Discovery.of(p.type(), p)) //
						.collect(Collectors.toList());
				}
				catch (ClassCastException | ServiceConfigurationError e) {
					return Collections.emptyList();
				}
			}
		};
	}

	/**
	 * Creates a {@code Discoverer} from a function yielding already-constructed
	 * objects. Such a source cannot be lazy; prefer {@link #serviceLoader()}
	 * or an index-backed discoverer where laziness matters.
	 *
	 * @param func yields the implementations of a given type
	 * @return a discoverer wrapping that function
	 */
	static <T> Discoverer using(
		final Function<Class<T>, ? extends Iterable<T>> func)
	{
		return new Discoverer() {

			@Override
			@SuppressWarnings("unchecked")
			public <U> List<Discovery<U>> discover(final Class<U> c) {
				try {
					var itr = (Iterable<U>) func.apply((Class<T>) c);
					return StreamSupport.stream(itr.spliterator(), false) //
						.map(Discovery::of) //
						.collect(Collectors.toList());
				}
				catch (ClassCastException | ServiceConfigurationError e) {
					return Collections.emptyList();
				}
			}
		};
	}

	/**
	 * Gets every {@code Discoverer} provided via {@link ServiceLoader}.
	 * <p>
	 * This module declares {@code uses org.scijava.discovery.Discoverer}, so -
	 * unlike discovery of arbitrary types - it can perform this lookup itself.
	 * </p>
	 *
	 * @return the provided discoverers
	 */
	static List<Discoverer> all() {
		try {
			return ServiceLoader.load(Discoverer.class).stream() //
				.map(ServiceLoader.Provider::get) //
				.collect(Collectors.toList());
		}
		catch (final ServiceConfigurationError e) {
			return Collections.emptyList();
		}
	}

	/**
	 * Gets every {@code Discoverer} provided via {@link ServiceLoader}, plus the
	 * given one.
	 *
	 * @param base the discoverer to append
	 * @return the provided discoverers, plus {@code base}
	 */
	static List<Discoverer> all(final Discoverer base) {
		final List<Discoverer> discoverers = new ArrayList<>(all());
		discoverers.add(base);
		return discoverers;
	}

	/**
	 * Combines several discoverers into one.
	 *
	 * @param discoverers the discoverers to combine
	 * @return a discoverer yielding the discoveries of all of them
	 */
	static Discoverer union(final Iterable<Discoverer> discoverers) {
		return new Discoverer() {

			@Override
			public <U> List<Discovery<U>> discover(final Class<U> c) {
				final List<Discovery<U>> list = new ArrayList<>();
				for (var discoverer : discoverers) {
					list.addAll(discoverer.discover(c));
				}
				return list;
			}
		};
	}

	/**
	 * Restricts this discoverer to the given types.
	 *
	 * @param classes the types to allow
	 * @return the restricted discoverer
	 */
	default Discoverer onlyFor(final Class<?>... classes) {
		var list = Arrays.asList(classes);
		var d = this;
		return new Discoverer() {

			@Override
			public <U> List<Discovery<U>> discover(final Class<U> c) {
				if (list.contains(c)) return d.discover(c);
				return Collections.emptyList();
			}
		};
	}

	/**
	 * Blocks this discoverer from discovering the given types.
	 *
	 * @param classes the types to block
	 * @return the restricted discoverer
	 */
	default Discoverer except(final Class<?>... classes) {
		var list = Arrays.asList(classes);
		var d = this;
		return new Discoverer() {

			@Override
			public <U> List<Discovery<U>> discover(final Class<U> c) {
				if (list.contains(c)) return Collections.emptyList();
				return d.discover(c);
			}
		};
	}

	/**
	 * Discovers the greatest implementation of the given comparable type.
	 *
	 * @param c the type of interest
	 * @return the greatest implementation found, if any
	 */
	default <U extends Comparable<U>> Optional<U> discoverMax(final Class<U> c) {
		// NB: natural order sorts in ascending order
		return instances(c).stream().max(Comparator.naturalOrder());
	}

	/**
	 * Discovers the least implementation of the given comparable type.
	 *
	 * @param c the type of interest
	 * @return the least implementation found, if any
	 */
	default <U extends Comparable<U>> Optional<U> discoverMin(final Class<U> c) {
		// NB: natural order sorts in ascending order
		return instances(c).stream().min(Comparator.naturalOrder());
	}
}
