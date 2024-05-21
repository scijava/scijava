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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.function.Container;
import org.scijava.function.Mutable;
import org.scijava.ops.engine.util.internal.AnnotationUtils;
import org.scijava.ops.spi.Nullable;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Structs;
import org.scijava.types.Types;
import org.scijava.types.inference.FunctionalInterfaces;

public final class FunctionalParameters {

	private FunctionalParameters() {
		// Prevent instantiation of static utility class
	}

	public static void parseFunctionalParameters(
		final ArrayList<SynthesizedParameterMember<?>> items, Type type,
		ParameterData data)
	{
		// Search for the functional method of 'type' and map its signature to
		// ItemIO
		List<FunctionalMethodType> fmts = FunctionalParameters
			.findFunctionalMethodTypes(type);

		// Synthesize members
		List<SynthesizedParameterMember<?>> fmtMembers = data.synthesizeMembers(
			fmts);

		for (SynthesizedParameterMember<?> m : fmtMembers) {
			final Class<?> itemType = Types.raw(m.getType());
			if ((m.getIOType() == ItemIO.MUTABLE || m
				.getIOType() == ItemIO.CONTAINER) && Structs.isImmutable(itemType))
			{
				// NB: The MUTABLE and CONTAINER types signify that the parameter
				// will be written to, but immutable parameters cannot be changed in
				// such a manner, so it makes no sense to label them as such.
				throw new IllegalArgumentException("Immutable " + m.getIOType() +
					" parameter: " + m.getKey() + " (" + itemType.getName() +
					" is immutable)");
			}
			items.add(m);
		}
	}

	/**
	 * Returns a list of {@link FunctionalMethodType}s describing the input and
	 * output types of the functional method of the specified functional type. In
	 * doing so, the return type of the method will me marked as
	 * {@link ItemIO#OUTPUT} and the all method parameters as
	 * {@link ItemIO#OUTPUT}, except for parameters annotated with
	 * {@link Container} or {@link Mutable} which will be marked as
	 * {@link ItemIO#CONTAINER} or {@link ItemIO#MUTABLE} respectively. If the
	 * specified type does not have a functional method in its hierarchy,
	 * {@code null} will be returned.<br>
	 * The order will be the following: method parameters from left to right, then
	 * return type.
	 *
	 * @param functionalType
	 * @return
	 */
	public static List<FunctionalMethodType> findFunctionalMethodTypes(
		Type functionalType)
	{
		Method functionalMethod = FunctionalInterfaces.functionalMethodOf(
			functionalType);
		if (functionalMethod == null) throw new IllegalArgumentException("Type " +
			functionalType +
			" is not a functional type, thus its functional method types cannot be determined");

		Type paramfunctionalType = functionalType;
		if (functionalType instanceof Class) {
			paramfunctionalType = Types.parameterizeRaw((Class<?>) functionalType);
		}

		List<FunctionalMethodType> out = new ArrayList<>();
		int i = 0;
		for (Type t : Types.getExactParameterTypes(functionalMethod,
			paramfunctionalType))
		{
			final ItemIO ioType;
			if (AnnotationUtils.getMethodParameterAnnotation(functionalMethod, i,
				Container.class) != null) ioType = ItemIO.CONTAINER;
			else if (AnnotationUtils.getMethodParameterAnnotation(functionalMethod, i,
				Mutable.class) != null) ioType = ItemIO.MUTABLE;
			else ioType = ItemIO.INPUT;
			out.add(new FunctionalMethodType(t, ioType));
			i++;
		}

		Type returnType = Types.getExactReturnType(functionalMethod,
			paramfunctionalType);
		if (!returnType.equals(void.class)) {
			out.add(new FunctionalMethodType(returnType, ItemIO.OUTPUT));
		}

		return out;
	}

	public static Boolean hasNullableAnnotations(Method m) {
		return Arrays.stream(m.getParameters()).anyMatch(p -> p.isAnnotationPresent(
			Nullable.class));
	}

	public static Boolean[] findParameterNullability(Method m) {
		return Arrays.stream(m.getParameters()).map(p -> p.isAnnotationPresent(
			Nullable.class)).toArray(Boolean[]::new);
	}

	public static List<Method> fMethodsWithNullable(Class<?> opClass) {
		Method superFMethod = FunctionalInterfaces.functionalMethodOf(opClass);
		return Arrays.stream(opClass.getMethods()) //
			.filter(m -> m.getName().equals(superFMethod.getName())) //
			.filter(m -> m.getParameterCount() == superFMethod.getParameterCount()) //
			.filter(m -> hasNullableAnnotations(m)) //
			.collect(Collectors.toList());
	}

	public static Boolean[] generateAllRequiredArray(int num) {
		Boolean[] arr = new Boolean[num];
		Arrays.fill(arr, false);
		return arr;
	}

}
