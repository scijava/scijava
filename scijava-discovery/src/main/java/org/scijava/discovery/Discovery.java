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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.scijava.priority.Priority;

/**
 * A single thing found by a {@link Discoverer}.
 * <p>
 * A {@code Discovery} is a <em>descriptor</em>, not the object itself. This
 * matters because the cheapest metadata sources - notably a generated
 * annotation index - can report what is available, and what its attributes
 * are, <em>without loading any classes</em>. Callers that only need to filter,
 * sort or display the available implementations therefore need not pay for
 * constructing them, and menu-style user interfaces can be built without
 * loading the classes behind the menu items.
 * </p>
 * <p>
 * The three access levels, in increasing order of cost:
 * </p>
 * <ol>
 * <li>{@link #implClassName()} and {@link #attrs()} - no class loading.</li>
 * <li>{@link #type()} - loads the class.</li>
 * <li>{@link #get()} - instantiates the object.</li>
 * </ol>
 *
 * @param <T> the type of the discovered object
 * @author Curtis Rueden
 */
public interface Discovery<T> {

	/**
	 * Gets the fully qualified name of the discovered implementation class.
	 * Implementations must be able to answer this <em>without</em> loading the
	 * class.
	 *
	 * @return the name of the implementation class
	 */
	String implClassName();

	/**
	 * Gets the discovered implementation class, loading it if necessary.
	 *
	 * @return the implementation class
	 */
	Class<? extends T> type();

	/**
	 * Gets the discovered object, constructing it if necessary.
	 *
	 * @return the discovered object
	 */
	T get();

	/**
	 * Gets the metadata associated with this discovery - for example a menu
	 * path or a label. Implementations must be able to answer this
	 * <em>without</em> loading the implementation class.
	 *
	 * @return the metadata, which may be empty but is never {@code null}
	 */
	default Map<String, String> attrs() {
		return Collections.emptyMap();
	}

	/**
	 * Gets the value of a single metadata attribute.
	 *
	 * @param key the attribute name
	 * @return the attribute value, if present
	 */
	default Optional<String> attr(final String key) {
		return Optional.ofNullable(attrs().get(key));
	}

	/**
	 * Gets the priority of this discovery, used to sort discoveries without
	 * constructing them. See {@link Priority} for the standard constants.
	 *
	 * @return the priority
	 */
	default double priority() {
		return Priority.NORMAL;
	}

	/**
	 * Creates a {@code Discovery} wrapping an object that has already been
	 * constructed.
	 *
	 * @param instance the object
	 * @return a discovery of that object
	 */
	static <T> Discovery<T> of(final T instance) {
		@SuppressWarnings("unchecked")
		final Class<? extends T> type = (Class<? extends T>) instance.getClass();
		return of(type, () -> instance);
	}

	/**
	 * Creates a {@code Discovery} of an already-loaded class, constructed on
	 * demand.
	 *
	 * @param type the implementation class
	 * @param supplier constructs the object
	 * @return a discovery of that class
	 */
	static <T> Discovery<T> of(final Class<? extends T> type,
		final Supplier<? extends T> supplier)
	{
		return of(type, supplier, Collections.emptyMap(), Priority.NORMAL);
	}

	/**
	 * Creates a {@code Discovery} of an already-loaded class, constructed on
	 * demand, with metadata.
	 *
	 * @param type the implementation class
	 * @param supplier constructs the object
	 * @param attrs the metadata
	 * @param priority the priority
	 * @return a discovery of that class
	 */
	static <T> Discovery<T> of(final Class<? extends T> type,
		final Supplier<? extends T> supplier, final Map<String, String> attrs,
		final double priority)
	{
		return new Discovery<>() {

			@Override
			public String implClassName() {
				return type.getName();
			}

			@Override
			public Class<? extends T> type() {
				return type;
			}

			@Override
			public T get() {
				return supplier.get();
			}

			@Override
			public Map<String, String> attrs() {
				return attrs;
			}

			@Override
			public double priority() {
				return priority;
			}

			@Override
			public String toString() {
				return implClassName();
			}
		};
	}

	/**
	 * Creates a {@code Discovery} of a class that has not been loaded yet. The
	 * class is loaded only when {@link #type()} or {@link #get()} is called,
	 * and constructed via its no-argument constructor.
	 * <p>
	 * This is the factory for index-backed discovery mechanisms, where the
	 * whole point is to report what is available without loading it.
	 * </p>
	 *
	 * @param implClassName the name of the implementation class
	 * @param superType the type the implementation class is assignable to
	 * @param classLoader the class loader with which to load the class
	 * @param attrs the metadata
	 * @param priority the priority
	 * @return a lazy discovery of that class
	 */
	static <T> Discovery<T> of(final String implClassName,
		final Class<T> superType, final ClassLoader classLoader,
		final Map<String, String> attrs, final double priority)
	{
		return new Discovery<>() {

			private Class<? extends T> type;

			@Override
			public String implClassName() {
				return implClassName;
			}

			@Override
			public synchronized Class<? extends T> type() {
				if (type == null) {
					try {
						type = Class.forName(implClassName, false, classLoader) //
							.asSubclass(superType);
					}
					catch (final ClassNotFoundException exc) {
						throw new IllegalStateException( //
							"Cannot load discovered class: " + implClassName, exc);
					}
				}
				return type;
			}

			@Override
			public T get() {
				try {
					return type().getDeclaredConstructor().newInstance();
				}
				catch (final ReflectiveOperationException exc) {
					throw new IllegalStateException( //
						"Cannot instantiate discovered class: " + implClassName, exc);
				}
			}

			@Override
			public Map<String, String> attrs() {
				return attrs;
			}

			@Override
			public double priority() {
				return priority;
			}

			@Override
			public String toString() {
				return implClassName;
			}
		};
	}
}
