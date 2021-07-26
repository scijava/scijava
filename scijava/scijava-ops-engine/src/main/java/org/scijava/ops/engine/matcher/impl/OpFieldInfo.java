/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.scijava.Priority;
import org.scijava.ValidityProblem;
import org.scijava.ops.engine.Hints;
import org.scijava.ops.engine.OpHints;
import org.scijava.ops.engine.OpInfo;
import org.scijava.ops.engine.OpUtils;
import org.scijava.ops.engine.hint.ImmutableHints;
import org.scijava.ops.engine.struct.FieldParameterMemberParser;
import org.scijava.ops.spi.OpField;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.struct.Structs;
import org.scijava.struct.ValidityException;

/**
 * Metadata about an op implementation defined as a field.
 * 
 * @author Curtis Rueden
 */
public class OpFieldInfo implements OpInfo {

	private final Object instance;
	private final Field field;
	private final List<String> names;

	private Struct struct;
	private ValidityException validityException;

	private final Hints hints;

	public OpFieldInfo(final Object instance, final Field field, final String... names) {
		this.instance = instance;
		this.field = field;
		this.names = Arrays.asList(names);

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
		List<ValidityProblem> problems = new ArrayList<>();
		// Reject all non public fields
		if (!Modifier.isPublic(field.getModifiers())) {
			problems.add(new ValidityProblem("Field to parse: " + field + " must be public."));
		}

		// NB: Subclassing a collection and inheriting its fields is NOT
		// ALLOWED!
		try {
			struct = Structs.from(field, problems, new FieldParameterMemberParser());
//			struct = ParameterStructs.structOf(field);
			OpUtils.checkHasSingleOutput(struct);
			// NB: Contextual parameters not supported for now.
		} catch (ValidityException e) {
			problems.addAll(e.problems());
		}
		if (!problems.isEmpty()) {
			validityException = new ValidityException(problems);
		}

		hints = formHints(field.getAnnotation(OpHints.class));
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
		final OpField opField = field.getAnnotation(OpField.class);
		return opField == null ? Priority.NORMAL : opField.priority();
	}

	@Override
	public String implementationName() {
		return field.getDeclaringClass().getName() + "." + field.getName();
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
	public ValidityException getValidityException() {
		return validityException;
	}

	@Override
	public boolean isValid() {
		return validityException == null;
	}
	
	@Override
	public AnnotatedElement getAnnotationBearer() {
		return field;
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
	public String toString() {
		return OpUtils.opString(this);
	}

	// -- Helper methods -- //

	private Hints formHints(OpHints h) {
		if (h == null) return new ImmutableHints(new String[0]);
		return new ImmutableHints(h.hints());
	}

}
