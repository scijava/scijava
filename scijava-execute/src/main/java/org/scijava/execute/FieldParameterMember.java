/*
 * #%L
 * Running things that declare their inputs and outputs.
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

package org.scijava.execute;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.scijava.common3.Types;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;
import org.scijava.struct.MemberInstance;
import org.scijava.struct.ValueAccessible;
import org.scijava.struct.ValueAccessibleMemberInstance;

/**
 * A {@link Member} backed by a {@link Parameter}-annotated field.
 *
 * @author Curtis Rueden
 */
public class FieldParameterMember<T> implements Member<T>, ValueAccessible<T> {

	private final Field field;
	private final Parameter parameter;
	private final Type itemType;

	public FieldParameterMember(final Field field, final Type structType) {
		this.field = field;
		this.parameter = field.getAnnotation(Parameter.class);
		if (parameter == null) {
			throw new IllegalArgumentException("Not a @Parameter field: " + field);
		}
		// NB: resolve against the declaring type, so that a generic field of a
		// parameterized class reports the type it actually has here.
		this.itemType = Types.typeOf(field, Types.raw(structType));
	}

	@Override
	public String key() {
		return field.getName();
	}

	@Override
	public String description() {
		return parameter.description();
	}

	@Override
	public Type type() {
		return itemType;
	}

	@Override
	public ItemIO getIOType() {
		return parameter.io();
	}

	@Override
	public boolean isRequired() {
		return parameter.required();
	}

	@Override
	public MemberInstance<T> createInstance(final Object o) {
		// NB: the default instance is read-only; this member can write, so it
		// must say so, or binding an input silently fails.
		return new ValueAccessibleMemberInstance<>(this, o);
	}

	/** Gets the label to display, falling back to the field name. */
	public String label() {
		return parameter.label().isEmpty() ? key() : parameter.label();
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get(final Object o) {
		try {
			field.setAccessible(true);
			return (T) field.get(o);
		}
		catch (final RuntimeException | IllegalAccessException exc) {
			throw new IllegalStateException(cannotAccess(o), exc);
		}
	}

	@Override
	public void set(final T value, final Object o) {
		try {
			field.setAccessible(true);
			field.set(o, value);
		}
		catch (final RuntimeException | IllegalAccessException exc) {
			throw new IllegalStateException(cannotAccess(o), exc);
		}
	}

	private String cannotAccess(final Object o) {
		// NB: the same qualified-opens requirement as everywhere else that
		// reflects into user classes.
		return "Cannot access " + field.getDeclaringClass().getName() + "." + //
			field.getName() + ". Does its module declare `opens " + field
				.getDeclaringClass().getPackageName() + " to org.scijava.execute;`?";
	}
}
