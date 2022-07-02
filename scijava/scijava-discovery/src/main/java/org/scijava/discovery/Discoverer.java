package org.scijava.discovery;

import java.lang.reflect.Type;
import java.util.*;
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
	 * @param c   the {@link Class} being searched for
	 * @return a {@link List} of implementations of {@code c}
	 */
	<U> List<U> discover(Class<U> c);

	/**
	 * Creates a {@link Discoverer} operating via {@link ServiceLoader}.
	 * 
	 * NB: This the {@link Function} input is <b>extremely</b> important. This puts
	 * the code loading the services into the user's module, allowing access to all
	 * of the services exposed to (and {@code use}d by) the calling module.
	 * Otherwise, all service interfaces would have to be {@code use}d by
	 * <b>this</b> module, which is not extensible.
	 * 
	 * @param func the {@link Function} generating a {@link ServiceLoader}
	 * @param <T>  the {@link Class} we attempt to discover, and consequently the
	 *             supertype of all implementations returned by
	 *             {@link ServiceLoader}
	 * @return A {@link Discoverer} backed by {@link ServiceLoader}
	 */
	static <T> Discoverer using(Function<Class<T>, ? extends ServiceLoader<T>> func) {
		return new Discoverer() {

			@Override
			public <U> List<U> discover(Class<U> c) {
				// If we can use c, look up the implementations
				try {
					Iterable<U> itr = (ServiceLoader<U>) func.apply((Class<T>) c);
					return StreamSupport.stream(itr.spliterator(), false).collect(Collectors.toList());
				} catch (ClassCastException e) {
					return Collections.emptyList();
				} catch (ServiceConfigurationError e) {
					return Collections.emptyList();
				}
			}
		};
	}

	/**
	 * Gets all {@link Discoverer}s made available through {@link ServiceLoader}, as
	 * well as a {@link Discoverer} that is itself backed by {@link ServiceLoader}.
	 * <p>
	 * It is <b>highly</b> recommended to call this method using <code>
	 *   List<Discoverer> discoverers = Discoverers.all(ServiceLoader::load);
	 * </code>
	 * 
	 * @param func A callbacks used to get the module scope of the caller. Through
	 *             {@code func}, the {@link ServiceLoader}-based {@link Discoverer}
	 *             is capable of discovering all interfaces `use`d by the caller
	 *             module. If we instead called {@link ServiceLoader#load(Class)}
	 *             directly, we'd only be able to discover implmentations whose
	 *             interface was `use`d by `module org.scijava.discovery`.
	 *             <p>
	 *             It is in the user's best interest to make this {@link Function}
	 *             as general as possible.
	 *
	 * @param <T>
	 * @return
	 */
	static <T> List<Discoverer> all(Function<Class<T>, ? extends ServiceLoader<T>> func) {
		// First, create the general-purpose Discoverer using the using(Function<...>)
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
	 * Accumulates mutiple {@link Discoverer}s into one mega-{@code Discoverer}
	 * 
	 * @param discoverers the {@link Discoverer}s to be wrapped
	 * @return the mega-{@link Discoverer}
	 */
	public static Discoverer union(Iterable<Discoverer> discoverers) {

		return new Discoverer() {
			@Override
			public <U> List<U> discover(Class<U> c) {
				return StreamSupport //
						.stream(discoverers.spliterator(), true) //
						.flatMap(d ->  {
							try {
								List<U> discoveries;
								discoveries = d.discover(c);
								return d.discover(c).stream();
							} catch(ClassCastException e) {
								return Stream.empty();
							}
						}) //
						.collect(Collectors.toList());
			}
		};
	}

	/**
	 * Wraps up this {@code Discoverer} into a {@link Discoverer} that <b>only</b> discoverers
	 * classes {@code classes}
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
				if (list.contains(c))
					return d.discover(c);
				return Collections.emptyList();
			}
		};
	}

	/**
	 * Wraps up this {@code Discoverer} into a {@link Discoverer} that <b>only</b> discoverers
	 * classes {@code classes}
	 *
	 * @param classes the {@link Class}es
	 * @return the wrapping
	 */
	default Discoverer except(Class<?>... classes) {
		List<Class<?>> list = Arrays.asList(classes);
		Discoverer d = this;
		return new Discoverer () {

			@Override
			public <U> List<U> discover(Class<U> c) {
				if (list.contains(c))
					return Collections.emptyList();
				return d.discover(c);
			}
		};
	}
}
