/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2016 Board of Regents of the University of
 * Wisconsin-Madison and University of Konstanz.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.scijava.core.Priority;
import org.scijava.ops.OpMethod;
import org.scijava.ops.OpUtils;
import org.scijava.param.ParameterStructs;
import org.scijava.param.ValidityException;
import org.scijava.param.ValidityProblem;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;

/**
 * @author Marcel Wiedenmann
 */
public class OpMethodInfo implements OpInfo {

	private final Method method;
	private Struct struct;
	private final ValidityException validityException;
	private Object instance;

	public OpMethodInfo(final Method method) {
		final List<ValidityProblem> problems = new ArrayList<>();
		// Reject all non public methods
		if (!Modifier.isPublic(method.getModifiers())) {
			problems.add(new ValidityProblem("Method to parse: " + method +
				" must be public."));
		}
		if (Modifier.isStatic(method.getModifiers())) {
			// TODO: We can't properly infer the generic types of static methods at
			// the moment. This might be a Java limitation.
			problems.add(new ValidityProblem("Method to parse: " + method +
				" must not be static."));
		}
		this.method = method;
		try {
			struct = ParameterStructs.structOf(method.getDeclaringClass(), method);
			instance = method.getDeclaringClass().getDeclaredConstructor()
				.newInstance();
		}
		catch (final ValidityException e) {
			problems.addAll(e.problems());
		}
		catch (NoSuchMethodException | InstantiationException
				| IllegalAccessException | InvocationTargetException e)
		{
			problems.add(new ValidityProblem("Could not instantiate method's class.",
				e));
		}
		validityException = problems.isEmpty() ? null : new ValidityException(
			problems);
	}

	// -- OpInfo methods --

	@Override
	public Type opType() {
		return method.getGenericReturnType();
	}

	@Override
	public Struct struct() {
		return struct;
	}

	@Override
	public double priority() {
		final OpMethod opMethod = method.getAnnotation(OpMethod.class);
		return opMethod == null ? Priority.NORMAL : opMethod.priority();
	}

	@Override
	public String implementationName() {
		// TODO: This includes all of the modifiers etc. of the method which are not
		// necessary to identify it. Use something custom? We need to be careful
		// because of method overloading, so just using the name is not sufficient.
		return method.toGenericString();
	}

	@Override
	public StructInstance<?> createOpInstance(
		final List<? extends Object> dependencies)
	{
		try {
			method.setAccessible(true);
			return struct.createInstance(method.invoke(instance, dependencies
				.toArray()));
		}
		catch (final IllegalAccessException | IllegalArgumentException
				| InvocationTargetException ex)
		{
			throw new IllegalStateException("Failed to invoke Op method: " + method +
				". Provided Op dependencies were: " + Objects.toString(dependencies),
				ex);
		}
	}

	@Override
	public boolean isValid() {
		return validityException == null;
	}

	@Override
	public ValidityException getValidityException() {
		return validityException;
	}

	// -- Object methods --

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof OpMethodInfo)) return false;
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
