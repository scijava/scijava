/*
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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

package org.scijava.ops.engine.matcher.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.scijava.meta.Versions;
import org.scijava.ops.api.Hints;
import org.scijava.ops.engine.OpDescription;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.exceptions.impl.PrivateOpException;
import org.scijava.ops.engine.struct.FieldInstance;
import org.scijava.ops.engine.struct.FieldParameterMemberParser;
import org.scijava.ops.engine.util.Infos;
import org.scijava.ops.spi.OpField;
import org.scijava.priority.Priority;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.struct.Structs;
import org.scijava.types.Types;

/**
 * Metadata about an Op implementation defined as a field.
 * 
 * @author Curtis Rueden
 */
public class OpFieldInfo implements OpInfo {

	private final Object instance;
	private final Field field;
	private final String version;
	private final List<String> names;
	private final double priority;

	private Struct struct;
	private final Hints hints;

	public OpFieldInfo(final Object instance, final Field field, final Hints hints, final String... names) {
		this(instance, field, Versions.getVersion(field.getDeclaringClass()), hints, Priority.NORMAL, names);
	}

	public OpFieldInfo(final Object instance, final Field field, final Hints hints, final double priority, final String... names) {
		this(instance, field, Versions.getVersion(field.getDeclaringClass()), hints, priority, names);
	}

	public OpFieldInfo(final Object instance, final Field field, final String version, final Hints hints, final String... names) {
		this(instance, field, version, hints, Priority.NORMAL, names);
	}

	public OpFieldInfo(final Object instance, final Field field, final String version, final Hints hints, final double priority, final String... names) {
		this.instance = instance;
		this.version = version;
		this.field = field;
		this.names = Arrays.asList(names);
		this.priority = priority;
		this.hints = hints;

		if (Modifier.isStatic(field.getModifiers())) {
			// Field is static; instance must be null.
			if (instance != null) {
				// Static field; instance should be null!
			}
		}
		else {
			// NB: Field is not static; instance must match field.getDeclaringClass().
			if (!field.getDeclaringClass().isInstance(instance)) {
				// Mismatch between given object and the class containing the field
				// But: we need to have proper case logic for the field being static or not.
			}
		}
		// Reject all non public fields
		if (!Modifier.isPublic(field.getModifiers())) {
			throw new PrivateOpException(field);
		}

		// NB: Subclassing a collection and inheriting its fields is NOT
		// ALLOWED!
		Type structType = Types.fieldType(field, field.getDeclaringClass());
		FieldInstance fieldInstance = new FieldInstance(field, instance);
		struct = Structs.from(fieldInstance, structType, new FieldParameterMemberParser());
		Infos.validate(this);
	}

	// -- OpInfo methods --

	@Override
	public List<String> names() {
		return names;
	}

	@Override
	public Type opType() {
		return field.getGenericType();
		// return Types.fieldType(field, subClass);
	}

	@Override
	public Struct struct() {
		return struct;
	}

	@Override
	public Hints declaredHints() {
		return hints;
	}

	@Override
	public double priority() {
		return priority;
	}

	@Override
	public String implementationName() {
		// Get generic string without modifiers and return type
		String fullyQualifiedField = field.toGenericString();
		int lastDotPos = fullyQualifiedField.lastIndexOf('.');
		fullyQualifiedField = fullyQualifiedField.substring(0, lastDotPos) + "$" + fullyQualifiedField.substring(lastDotPos + 1);
		String packageName = field.getDeclaringClass().getPackageName();
		int classNameIndex = fullyQualifiedField.lastIndexOf(packageName);
		return fullyQualifiedField.substring(classNameIndex);
	}

	@Override
	public StructInstance<?> createOpInstance(List<?> dependencies)
	{
		if (dependencies != null && !dependencies.isEmpty())
			throw new IllegalArgumentException(
				"Op fields are not allowed to have any Op dependencies.");
		// NB: In general, there is no way to create a new instance of the field value.
		// Calling clone() may or may not work; it does not work with e.g. lambdas.
		// Better to just use the same value directly, rather than trying to copy.
		try {
			final Object object = field.get(instance);
			// TODO: Wrap object in a generic holder with the same interface.
			return struct().createInstance(object);
		} catch (final IllegalAccessException exc) {
			// FIXME
			exc.printStackTrace();
			throw new RuntimeException(exc);
		}
	}

	@Override
	public AnnotatedElement getAnnotationBearer() {
		return field;
	}

	@Override
	public String version() {
		return version;
	}

	/**
	 * For an {@link OpField}, we define the implementation as the concatenation
	 * of:
	 * <ol>
	 * <li>The fully qualified name of the class containing the field</li>
	 * <li>The method field</li>
	 * <li>The version of the class containing the field, with a preceding
	 * {@code @}</li>
	 * </ol>
	 * <p>
	 * For example, for a field {@code baz} in class
	 * {@code com.example.foo.Bar}, you might have
	 * <p>
	 * {@code com.example.foo.Bar.baz@1.0.0}
	 * <p>
	 */
	@Override
	public String id() {
		return OpInfo.IMPL_DECLARATION + implementationName() + "@" + version();
	}

	// -- Object methods --

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof OpFieldInfo))
			return false;
		final OpInfo that = (OpInfo) o;
		return struct().equals(that.struct());
	}

	@Override
	public int hashCode() {
		return struct().hashCode();
	}

	@Override
	public String toString() { return OpDescription.verbose(this); }

}
