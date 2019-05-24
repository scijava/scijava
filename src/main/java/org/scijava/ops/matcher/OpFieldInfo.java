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

package org.scijava.ops.matcher;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.scijava.core.Priority;
import org.scijava.ops.OpField;
import org.scijava.ops.OpUtils;
import org.scijava.param.ParameterStructs;
import org.scijava.param.ValidityException;
import org.scijava.param.ValidityProblem;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;

/**
 * Metadata about an op implementation defined as a field.
 * 
 * @author Curtis Rueden
 */
public class OpFieldInfo implements OpInfo {

	private final Field field;
	private Struct struct;
	private ValidityException validityException;
	private Object instance;

	public OpFieldInfo(final Field field) {

		List<ValidityProblem> problems = new ArrayList<>();
		// Reject all non public fields
		if (!Modifier.isPublic(field.getModifiers())) {
			problems.add(new ValidityProblem("Field to parse: " + field + " must be public."));
		}

		// NB: Subclassing a collection and inheriting its fields is NOT
		// ALLOWED!
		this.field = field;
		try {
			struct = ParameterStructs.structOf(field.getDeclaringClass(), field);
			OpUtils.checkHasSingleOutput(struct);
			if (!Modifier.isStatic(field.getModifiers())) {
				instance = field.getDeclaringClass().newInstance();
			}
			// NB: Contextual parameters not supported for now.
		} catch (ValidityException e) {
			problems.addAll(e.problems());
		} catch (InstantiationException | IllegalAccessException e) {
			problems.add(new ValidityProblem("Could not instantiate field's class", e));
		}
		if (!problems.isEmpty()) {
			validityException = new ValidityException(problems);
		}
		
	}

	// -- OpInfo methods --

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
		// 1. Can we create another instance of the same function by calling
		// clone()?
		// 2. _SHOULD_ we do that? Or can we simply reuse the same function
		// instance every time?
		try {
			final Object object = field.get(instance); // NB: value of static field!
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
}
