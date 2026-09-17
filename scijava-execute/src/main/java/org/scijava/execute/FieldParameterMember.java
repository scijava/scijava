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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.scijava.common3.Types;
import org.scijava.struct.ItemIO;
import java.util.LinkedHashMap;
import java.util.Map;

import org.scijava.struct.MemberInstance;
import org.scijava.struct.ValueAccessible;
import org.scijava.struct.ValueAccessibleMemberInstance;

/**
 * A {@link Member} backed by a {@link Parameter}-annotated field.
 *
 * @author Curtis Rueden
 */
public class FieldParameterMember<T> implements ParameterMember<T>,
	ValueAccessible<T>
{

	private final Field field;
	private final Parameter parameter;
	private final Type itemType;
	private final Lookup lookup;
	private VarHandle handle;

	public FieldParameterMember(final Field field, final Type structType) {
		this(field, structType, null);
	}

	/**
	 * Creates a member whose value is read and written through the given lookup.
	 * <p>
	 * NB: reflective access is checked against the module that performs it, so
	 * a lookup supplied by the container is what lets a plugin author open
	 * their package to the container alone, rather than to every SciJava module
	 * that might touch their fields.
	 * </p>
	 *
	 * @param field the annotated field
	 * @param structType the type declaring it
	 * @param lookup a lookup with private access to the declaring class, or
	 *          null to reflect with this module's own access
	 */
	public FieldParameterMember(final Field field, final Type structType,
		final Lookup lookup)
	{
		this.lookup = lookup;
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

	@Override
	public Map<String, String> attrs() {
		final Map<String, String> attrs = new LinkedHashMap<>();
		put(attrs, LABEL, parameter.label());
		put(attrs, CALLBACK, parameter.callback());
		put(attrs, VALIDATOR, parameter.validator());
		put(attrs, VISIBLE_WHEN, parameter.visibleWhen());
		put(attrs, GROUP, parameter.group());
		return attrs;
	}

	private static void put(final Map<String, String> attrs, final String key,
		final String value)
	{
		if (!value.isEmpty()) attrs.put(key, value);
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get(final Object o) {
		return (T) handle().get(o);
	}

	@Override
	public void set(final T value, final Object o) {
		handle().set(o, value);
	}

	/** Gets the handle for this field, resolving it on first use. */
	private synchronized VarHandle handle() {
		if (handle == null) {
			try {
				final Lookup access = lookup != null ? lookup //
					: ownLookupIn(field.getDeclaringClass());
				handle = access.unreflectVarHandle(field);
			}
			catch (final RuntimeException | IllegalAccessException exc) {
				throw new IllegalStateException(cannotAccess(), exc);
			}
		}
		return handle;
	}

	/**
	 * Gets a lookup into the given class using this module's own access.
	 * <p>
	 * NB: {@code privateLookupIn} requires this module to <em>read</em> the
	 * target's module as well as the package being open to it. A library never
	 * declares a read edge to its callers, so it has to add one at runtime -
	 * which only its own code may do. Callers with a container avoid all this
	 * by passing a lookup from there instead.
	 * </p>
	 */
	private static Lookup ownLookupIn(final Class<?> type)
		throws IllegalAccessException
	{
		FieldParameterMember.class.getModule().addReads(type.getModule());
		return MethodHandles.privateLookupIn(type, MethodHandles.lookup());
	}

	private String cannotAccess() {
		return "Cannot access " + field.getDeclaringClass().getName() + "." + //
			field.getName() + ". Does its module declare `opens " + field
				.getDeclaringClass().getPackageName() + " to org.scijava.context;`?" + //
			" A caller with no container must open to org.scijava.execute instead.";
	}
}
