/*-
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.scijava.ops.engine.exceptions.impl.FunctionalTypeOpException;
import org.scijava.struct.MemberParser;
import org.scijava.struct.Structs;
import org.scijava.types.Types;
import org.scijava.types.inference.FunctionalInterfaces;

public class ClassParameterMemberParser implements
	MemberParser<Class<?>, SynthesizedParameterMember<?>>
{

	@Override
	public List<SynthesizedParameterMember<?>> parse(Class<?> source,
		Type structType)
	{
		if (source == null) return null;

		final ArrayList<SynthesizedParameterMember<?>> items = new ArrayList<>();

		// NB: Reject abstract classes.
		Structs.checkModifiers(source.getName() + ": ", source.getModifiers(), true,
			Modifier.ABSTRACT);

		// Obtain source's Op method.
		Method opMethod;
		try {
			opMethod = getDeclaredOpMethod(source);
		}
		catch (NoSuchMethodException e1) {
			throw new IllegalArgumentException("Class " + source.getName() +
				" does not have a functional method!", e1);
		}

		// obtain a parameterData
		Class<?> fIface = FunctionalInterfaces.findFrom(source);
		ParameterData paramData = new SynthesizedMethodParameterData(opMethod,
			fIface);

		try {
			FunctionalParameters.parseFunctionalParameters(items, source, paramData);
		}
		catch (IllegalArgumentException exc) {
			throw new FunctionalTypeOpException(source, exc);
		}

		return items;
	}

	/**
	 * Returns the declared {@link FunctionalInterface} method implemented by the
	 * Op {@code c}, or, as a fallback, the functional method w.r.t. its
	 * declaration.
	 *
	 * @param c the Op {@link Class}
	 * @return the {@link Method} of the {@link FunctionalInterface} implemented
	 *         by {@code c}
	 * @throws NoSuchMethodException when {@code c} does not implement its
	 *           functional method
	 */
	private Method getDeclaredOpMethod(Class<?> c) throws NoSuchMethodException {
		// NB this is the functional method w.r.t. the interface, not w.r.t. the Op
		Method fMethod = FunctionalInterfaces.functionalMethodOf(c);
		Type[] paramTypes = Types.getExactParameterTypes(fMethod, c);
		Class<?>[] rawParamTypes = Arrays.stream(paramTypes).map(t -> Types.raw(t))
			.toArray(Class[]::new);
		try {
			return c.getMethod(fMethod.getName(), rawParamTypes);
		}
		catch (NoSuchMethodException e) {
			return fMethod;
		}
	}

}
