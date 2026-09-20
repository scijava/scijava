/*-
 * #%L
 * A library for building and introspecting structs.
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

package org.scijava.struct;

import java.lang.reflect.Field;

import org.scijava.common3.Types;

/**
 * {@link Member} backed by a {@link Field} annotated by {@link Parameter}.
 *
 * @author Curtis Rueden
 * @param <T>
 */
public class FieldParameterMember<T> extends AnnotatedParameterMember<T>
	implements ValueAccessible<T>
{

	private final Field field;
	private final Class<?> structType;
	private final Struct struct;

	public FieldParameterMember(final Field field, final Class<?> structType)
		throws ValidityException
	{
		super(Types.typeOf(field, structType), //
			field.getAnnotation(Parameter.class));
		this.field = field;
		this.structType = structType;
		struct = isStruct() ? ParameterStructs.structOf(rawType()) : null;
	}

	// -- FieldParameterItem methods --

	public Field getField() {
		return field;
	}

	// -- ValueAccessible methods --

	@Override
	public T get(final Object o) {
		try {
			@SuppressWarnings("unchecked")
			final T value = (T) ParameterStructs.field(this).get(o);
			return value;
		}
		catch (final IllegalAccessException exc) {
			return null; // FIXME
		}
	}

	@Override
	public void set(final T value, final Object o) {
		try {
			ParameterStructs.field(this).set(o, value);
		}
		catch (final IllegalAccessException exc) {
			// FIXME
		}
	}

	// -- ParameterItem methods --

	@Override
	public T getDefaultValue() {
		// NB: The default value is the initial field value.
		// E.g.:
		//
		//   @Parameter
		//   private int weekdays = 5;
		//
		// To obtain this information, we need to instantiate the object, then
		// extract the value of the associated field.
		//
		// Of course, the command might do evil things like:
		//
		//   @Parameter
		//   private long time = System.currentTimeMillis();
		//
		// In which case the default value will vary by instance. But there is
		// nothing we can really do about that. This is only a best effort.

		try {
			final Object dummy = structType.newInstance();
			@SuppressWarnings("unchecked")
			final T value = (T) getField().get(dummy);
			return value;
		}
		catch (final InstantiationException | IllegalAccessException exc) {
			throw new IllegalStateException("Missing no-args constructor", exc);
		}
	}

	// -- Member methods --

	@Override
	public String key() {
		final String key = getAnnotation().key();
		return key == null || key.isEmpty() ? field.getName() : key;
	}

	@Override
	public Struct childStruct() {
		return struct;
	}

	@Override
	public MemberInstance<T> createInstance(final Object o) {
		return new ValueAccessibleMemberInstance<>(this, o);
	}
}
