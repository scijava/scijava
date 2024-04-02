/*-
 * #%L
 * Plugin discovery subsystem for SciJava libraries.
 * %%
 * Copyright (C) 2021 - 2024 SciJava developers.
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
 * Discovers implementations of a given type.
 *
 * @author Gabriel Selzer
 */
@FunctionalInterface
public interface Discoverer {

	/**
	 * Discovers implementations of some {@link Class} {@code c}.
	 *
	 * @param <U> the {@link Type} of the {@link Class} being searched for
	 * @param c the {@link Class} being searched for
	 * @return a {@link List} of implementations of {@code c}
	 */
	<U> List<U> discover(Class<U> c);

	/**
	 * Creates a {@link Discoverer} operating via {@link ServiceLoader}. NB: This
	 * the {@link Function} input is <b>extremely</b> important. This puts the
	 * code loading the services into the user's module, allowing access to all of
	 * the services exposed to (and {@code use}d by) the calling module.
	 * Otherwise, all service interfaces would have to be {@code use}d by
	 * <b>this</b> module, which is not extensible.
	 *
	 * @param func the {@link Function} generating a {@link ServiceLoader}
	 * @param <T> the {@link Class} we attempt to discover, and consequently the
	 *          supertype of all implementations returned by {@link ServiceLoader}
	 * @return A {@link Discoverer} backed by {@link ServiceLoader}
	 */
	static <T> Discoverer using(
		Function<Class<T>, ? extends ServiceLoader<T>> func)
	{
		return new Discoverer() {

			@Override
			public <U> List<U> discover(Class<U> c) {
				// If we can use c, look up the implementations
				try {
					Iterable<U> itr = (ServiceLoader<U>) func.apply((Class<T>) c);
					return StreamSupport.stream(itr.spliterator(), false).collect(
						Collectors.toList());
				}
				catch (ClassCastException e) {
					return Collections.emptyList();
				}
				catch (ServiceConfigurationError e) {
					return Collections.emptyList();
				}
			}
		};
	}

	static Iterable<Discoverer> allProvided() {
		return ServiceLoader.load(Discoverer.class);
	}

	/**
	 * Gets all {@link Discoverer}s made available through {@link ServiceLoader},
	 * as well as a {@link Discoverer} that is itself backed by
	 * {@link ServiceLoader}.
	 * <p>
	 * It is <b>highly</b> recommended to call this method using {@code
	 *   List<Discoverer> discoverers = Discoverers.all(ServiceLoader::load);
	 * }
	 *
	 * @param func A callbacks used to get the module scope of the caller. Through
	 *          {@code func}, the {@link ServiceLoader}-based {@link Discoverer}
	 *          is capable of discovering all interfaces {@code use}d by the
	 *          caller module. If we instead called
	 *          {@link ServiceLoader#load(Class)} directly, we'd only be able to
	 *          discover implementations whose interface was {@code use}d by
	 *          {@code module org.scijava.discovery}.
	 *          <p>
	 *          It is in the user's best interest to make this {@link Function} as
	 *          general as possible.
	 * @param <T>
	 * @return
	 */
	static <T> List<Discoverer> all(
		Function<Class<T>, ? extends ServiceLoader<T>> func)
	{
		// First, create the general-purpose Discoverer using the
		// using(Function<...>)
		// method
		Discoverer d = using(func);

		// Then, use that Discoverer to discoverer all Discoverers provided to the
		// caller module
		Collection<Discoverer> allProvided = d.discover(Discoverer.class);

		// append the general purpose discoverer to the discovered Discoverers, and
		// return that.
		List<Discoverer> discoverers = new ArrayList<>(allProvided);
		discoverers.add(d);
		return discoverers;
	}

	/**
	 * Accumulates multiple {@link Discoverer}s into one mega-{@code Discoverer}
	 *
	 * @param discoverers the {@link Discoverer}s to be wrapped
	 * @return the mega-{@link Discoverer}
	 */
	public static Discoverer union(Iterable<Discoverer> discoverers) {

		return new Discoverer() {

			@Override
			public <U> List<U> discover(Class<U> c) {
				List<U> list = new ArrayList<>();
				for (var discoverer : discoverers) {
					list.addAll(discoverer.discover(c));
				}
				return list;
			}
		};
	}

	/**
	 * Wraps up this {@code Discoverer} into a {@link Discoverer} that <b>only</b>
	 * discoverers classes {@code classes}
	 *
	 * @param classes the {@link Class}es
	 * @return the wrapping
	 */
	default Discoverer onlyFor(Class<?>... classes) {
		List<Class<?>> list = Arrays.asList(classes);
		Discoverer d = this;
		return new Discoverer() {

			@Override
			public <U> List<U> discover(Class<U> c) {
				if (list.contains(c)) return d.discover(c);
				return Collections.emptyList();
			}
		};
	}

	/**
	 * Wraps up this {@code Discoverer} into a {@link Discoverer} that <b>only</b>
	 * discoverers classes {@code classes}
	 *
	 * @param classes the {@link Class}es
	 * @return the wrapping
	 */
	default Discoverer except(Class<?>... classes) {
		List<Class<?>> list = Arrays.asList(classes);
		Discoverer d = this;
		return new Discoverer() {

			@Override
			public <U> List<U> discover(Class<U> c) {
				if (list.contains(c)) return Collections.emptyList();
				return d.discover(c);
			}
		};
	}

	/**
	 * Finds the maximum implementation of any {@link Comparable} {@code c}.
	 *
	 * @param c the {@link Class}, extending {@link Comparable} that the returned
	 *          implementation <b>must</b> implement
	 * @param <U> the {@link Type} of {@code c}
	 * @return the maximum implementation of {@code c}
	 */
	default <U extends Comparable<U>> Optional<U> discoverMax(Class<U> c) {
		List<U> discoveries = discover(c);
		// NB: natural order sorts in ascending order
		return discoveries.stream().max(Comparator.naturalOrder());
	}

	/**
	 * Finds the minimum implementation of any {@link Comparable} {@code c}.
	 *
	 * @param c the {@link Class}, extending {@link Comparable} that the returned
	 *          implementation <b>must</b> implement
	 * @param <U> the {@link Type} of {@code c}
	 * @return the minimum implementation of {@code c}
	 */
	default <U extends Comparable<U>> Optional<U> discoverMin(Class<U> c) {
		List<U> discoveries = discover(c);
		// NB: natural order sorts in ascending order
		return discoveries.stream().min(Comparator.naturalOrder());
	}
}
