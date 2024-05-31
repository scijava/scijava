/*
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.ops.engine.struct;

import java.lang.reflect.Field;

import org.scijava.ops.spi.OpDependency;
import org.scijava.struct.MemberInstance;
import org.scijava.struct.ValueAccessible;
import org.scijava.struct.ValueAccessibleMemberInstance;
import org.scijava.common3.Types;

/**
 * @author Marcel Wiedenmann
 */
public class FieldOpDependencyMember<T> extends AnnotatedOpDependencyMember<T>
	implements ValueAccessible<T>
{

	private final Field field;

	public FieldOpDependencyMember(final Field field, final Class<?> structType) {
		super( //
			field.getName(), //
			"", //
			Types.typeOf(field, structType), //
			field.getAnnotation(OpDependency.class) //
		);
		this.field = field;
	}

	// -- ValueAccessible methods --

	@Override
	public T get(Object o) {
		field.setAccessible(true);
		try {
			@SuppressWarnings("unchecked")
			final T value = (T) field.get(o);
			return value;
		}
		catch (final IllegalAccessException exc) {
			// FIXME
			throw new RuntimeException(exc);
		}
	}

	@Override
	public void set(T value, Object o) {
		field.setAccessible(true);
		try {
			field.set(o, value);
		}
		catch (final IllegalAccessException exc) {
			// FIXME
			throw new RuntimeException(exc);
		}
	}

	// -- Member methods --

	@Override
	public MemberInstance<T> createInstance(final Object o) {
		return new ValueAccessibleMemberInstance<>(this, o);
	}
}
