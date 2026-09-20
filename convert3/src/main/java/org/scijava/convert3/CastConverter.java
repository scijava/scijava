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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import org.scijava.common3.Classes;
import org.scijava.common3.Types;
import org.scijava.priority.Priority;

/**
 * Converts a value that is already of the wanted type: it returns it.
 * <p>
 * NB: first, and so the answer whenever no conversion is needed at all. That
 * is the common case by far - most values arrive already right - and it costs
 * one assignability check.
 * </p>
 *
 * @author Curtis Rueden
 */
public class CastConverter implements Converter<Object, Object> {

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
		if (source == null || dest == null) return false;
		final Class<?> raw = Types.raw(dest);
		if (raw == null) return false;
		if (!Classes.box(raw).isInstance(source)) return false;
		// NB: a collection is only "already the right type" if what it holds is
		// too, and erasure means we cannot tell by looking at the type. A
		// List<String> handed to a List<File> would otherwise be returned
		// unchanged -- the right raw type full of the wrong things -- so the
		// element-wise converter gets it instead, and returns it untouched when
		// the elements do match.
		return !(source instanceof Collection && isParameterized(dest));
	}

	/** Gets whether the type says anything about what it contains. */
	private static boolean isParameterized(final Type dest) {
		if (!(dest instanceof ParameterizedType)) return false;
		for (final Type argument : ((ParameterizedType) dest)
			.getActualTypeArguments())
		{
			if (argument != Object.class) return true;
		}
		return false;
	}

	@Override
	public Object convert(final Object source, final Type dest) {
		return source;
	}

	@Override
	public double priority() {
		return Priority.VERY_HIGH;
	}
}
