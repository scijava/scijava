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
import java.util.function.Function;

import org.scijava.common3.Types;
import org.scijava.priority.Priority;

/**
 * Turns a value of one type into a value of another.
 * <p>
 * Contribute one through {@link java.util.ServiceLoader}: a module that
 * {@code provides org.scijava.convert3.Converter} is enough, and no container
 * is involved. That matters because conversion is needed <em>below</em> the
 * container - binding an input to a parameter converts, and a script or a
 * command line supplies strings for everything.
 * </p>
 * <p>
 * NB: SciJava Common's {@code Converter} had eight {@code canConvert}
 * overloads across {@code Object}/{@code Class}/{@code Type} and a
 * {@code ConversionRequest} to carry the combinations. Here there is one
 * question - can you turn <em>this value</em> into <em>that type</em> - and
 * the types are declared, so the common case needs no code at all:
 * </p>
 *
 * <pre>
 * Converter.of(String.class, Integer.class, Integer::valueOf)
 * </pre>
 *
 * @param <I> the type converted from
 * @param <O> the type converted to
 * @author Curtis Rueden
 */
public interface Converter<I, O> {

	/** Gets the type this converter accepts. */
	Type sourceType();

	/** Gets the type this converter produces. */
	Type destType();

	/**
	 * Converts the given value.
	 *
	 * @param source the value to convert, never null
	 * @param dest the type wanted, which {@link #supports} has accepted
	 * @return the converted value, or null if this converter cannot after all
	 */
	Object convert(Object source, Type dest);

	/**
	 * Gets whether this converter can turn the given value into the given type.
	 * <p>
	 * The default answers from the declared types: the value must be one this
	 * converter accepts, and what it produces must fit where the result is
	 * going.
	 * </p>
	 */
	default boolean supports(final Object source, final Type dest) {
		if (source == null || dest == null) return false;
		return Types.isAssignable(source.getClass(), sourceType()) && //
			Types.isAssignable(destType(), Types.raw(dest) == null ? dest //
				: org.scijava.common3.Classes.box(Types.raw(dest)));
	}

	/** Sorts converters, highest first. See {@link Priority}. */
	default double priority() {
		return Priority.NORMAL;
	}

	/**
	 * Creates a converter from a function.
	 *
	 * @param <I> the type converted from
	 * @param <O> the type converted to
	 * @param sourceType the type accepted
	 * @param destType the type produced
	 * @param function what does the converting
	 * @return the new converter
	 */
	static <I, O> Converter<I, O> of(final Class<I> sourceType,
		final Class<O> destType, final Function<I, O> function)
	{
		return of(sourceType, destType, function, Priority.NORMAL);
	}

	/** Creates a converter from a function, at the given priority. */
	static <I, O> Converter<I, O> of(final Class<I> sourceType,
		final Class<O> destType, final Function<I, O> function,
		final double priority)
	{
		return new Converter<>() {

			@Override
			public Type sourceType() {
				return sourceType;
			}

			@Override
			public Type destType() {
				return destType;
			}

			@Override
			@SuppressWarnings("unchecked")
			public Object convert(final Object source, final Type dest) {
				return function.apply((I) source);
			}

			@Override
			public double priority() {
				return priority;
			}

			@Override
			public String toString() {
				return Types.name(sourceType) + " -> " + Types.name(destType);
			}
		};
	}
}
