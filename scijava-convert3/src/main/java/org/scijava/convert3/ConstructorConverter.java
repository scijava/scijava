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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import org.scijava.common3.Classes;
import org.scijava.common3.Types;
import org.scijava.priority.Priority;

/**
 * Converts by handing the value to a constructor of the wanted type.
 * <p>
 * This is what turns a {@code String} into a {@code File}, a {@code URI} into
 * whatever wraps one, and a value into any class written to wrap it. It is
 * last, because it is a guess - a constructor taking the source type is not a
 * promise that the result means the same thing.
 * </p>
 *
 * @author Curtis Rueden
 */
public class ConstructorConverter implements Converter<Object, Object> {

	@Override
	public Type sourceType() {
		return Object.class;
	}

	@Override
	public Type destType() {
		return Object.class;
	}

	@Override
	public boolean supports(final Object source, final Type dest) {
		return source != null && constructor(source, dest) != null;
	}

	@Override
	public Object convert(final Object source, final Type dest) {
		final Constructor<?> ctor = constructor(source, dest);
		if (ctor == null) return null;
		try {
			return ctor.newInstance(source);
		}
		catch (final ReflectiveOperationException | IllegalArgumentException exc) {
			// NB: the constructor existed and refused the value; the caller sees
			// this as "cannot convert", which is what it amounts to.
			return null;
		}
	}

	@Override
	public double priority() {
		return Priority.VERY_LOW;
	}

	// -- Helper methods --

	/** Finds a public constructor of the destination taking the source. */
	private static Constructor<?> constructor(final Object source,
		final Type dest)
	{
		final Class<?> raw = Types.raw(dest);
		if (raw == null || raw.isInterface() || raw.isPrimitive() || //
			Modifier.isAbstract(raw.getModifiers())) return null;
		for (final Constructor<?> ctor : raw.getConstructors()) {
			if (ctor.getParameterCount() != 1) continue;
			final Class<?> param = Classes.box(ctor.getParameterTypes()[0]);
			if (param.isInstance(source)) return ctor;
		}
		return null;
	}
}
