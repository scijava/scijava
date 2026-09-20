/*
 * #%L
 * Converting a value to the type something else wants.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.convert3;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import org.scijava.common3.Classes;
import org.scijava.common3.Types;

/**
 * Converts values to the types other code wants, using the
 * {@link Converter}s available to the runtime.
 * <p>
 * Converters come from {@link ServiceLoader}, so this needs no container and
 * no configuration. For an embedded or tested setting where implicit discovery
 * is unwanted, construct an instance with an explicit list instead.
 * </p>
 *
 * @author Curtis Rueden
 */
public class Converters {

	private static class DefaultHolder {

		static final Converters INSTANCE = new Converters(discoverConverters());
	}

	private final List<Converter<?, ?>> converters;

	/** Creates an instance that uses exactly the given converters. */
	public Converters(final List<? extends Converter<?, ?>> converters) {
		this.converters = new ArrayList<>(converters);
		// NB: highest priority first, so the first supporting converter wins.
		this.converters.sort(Comparator.comparingDouble(
			(Converter<?, ?> c) -> c.priority()).reversed());
	}

	/** Gets the shared instance, backed by {@link ServiceLoader} discovery. */
	public static Converters get() {
		return DefaultHolder.INSTANCE;
	}

	/**
	 * Converts the given value to the given type.
	 *
	 * @param source the value to convert
	 * @param dest the type wanted
	 * @return the converted value
	 * @throws ConversionException if nothing can convert it
	 */
	public Object convert(final Object source, final Type dest) {
		return tryConvert(source, dest).orElseThrow(() -> ConversionException.of(
			source, dest));
	}

	/**
	 * Converts the given value to the given type.
	 *
	 * @param <T> the type wanted
	 * @param source the value to convert
	 * @param dest the type wanted
	 * @return the converted value
	 * @throws ConversionException if nothing can convert it
	 */
	public <T> T convert(final Object source, final Class<T> dest) {
		@SuppressWarnings("unchecked")
		final T result = (T) convert(source, (Type) dest);
		return result;
	}

	/**
	 * Converts the given value, if anything can.
	 * <p>
	 * NB: this is the form for a user interface, where a half-typed value is
	 * not an error but simply not a value yet. A caller that needs the
	 * conversion to happen should use {@link #convert} and let it throw.
	 * </p>
	 *
	 * @param source the value to convert
	 * @param dest the type wanted
	 * @return the converted value, or empty if nothing can convert it, or if
	 *         the conversion itself declined
	 */
	public Optional<Object> tryConvert(final Object source, final Type dest) {
		if (dest == null) return Optional.empty();
		if (source == null) {
			// NB: null converts to null, except for a primitive, which cannot
			// hold one: there, the type's own zero is the nearest thing.
			final Class<?> raw = Types.raw(dest);
			return raw != null && raw.isPrimitive() //
				? Optional.ofNullable(Classes.nullValue(raw)) : Optional.empty();
		}
		for (final Converter<?, ?> converter : converters) {
			if (!converter.supports(source, dest)) continue;
			final Object result = converter.convert(source, dest);
			if (result != null) return Optional.of(result);
			// NB: a converter that accepted the value and then produced nothing -
			// unparseable text, most often - lets the next one try.
		}
		return Optional.empty();
	}

	/** Converts the given value, if anything can. */
	public <T> Optional<T> tryConvert(final Object source, final Class<T> dest) {
		@SuppressWarnings("unchecked")
		final Optional<T> result = (Optional<T>) tryConvert(source, (Type) dest);
		return result;
	}

	/** Gets whether the given value can be converted to the given type. */
	public boolean supports(final Object source, final Type dest) {
		if (dest == null) return false;
		if (source == null) {
			final Class<?> raw = Types.raw(dest);
			return raw != null && raw.isPrimitive();
		}
		return converters.stream().anyMatch(c -> c.supports(source, dest));
	}

	/** Gets the converters in use, highest priority first. */
	public List<Converter<?, ?>> converters() {
		return List.copyOf(converters);
	}

	// -- Helper methods --

	private static List<Converter<?, ?>> discoverConverters() {
		final List<Converter<?, ?>> found = new ArrayList<>();
		// NB: loaded here, in this module, because ServiceLoader resolves `uses`
		// against the calling module.
		ServiceLoader.load(Converter.class).forEach(found::add);
		return found;
	}
}
