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
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import org.scijava.ops.engine.exceptions.impl.NullablesOnMultipleMethodsException;
import org.scijava.ops.spi.OpDependency;
import org.scijava.struct.ItemIO;

/**
 * Lazily generates the parameter data for a {@link List} of
 * {@link FunctionalMethodType}s. <b>If</b> there exists a <b>full</b> set of
 * {@code @param} and {@code @return} tags, the javadoc will be used to create
 * the parameter names and descriptions. Otherwise, reasonable defaults will be
 * used.
 *
 * @author Gabriel Selzer
 */
public class SynthesizedMethodParameterData implements ParameterData {

	private final Method m;
	private final Class<?> opType;

	public SynthesizedMethodParameterData(Method m, Class<?> opType) {
		this.m = m;
		this.opType = opType;
	}

	private List<String> getParameterNames(List<FunctionalMethodType> fmts) {
		List<String> fmtNames = new ArrayList<>(fmts.size());
		int ins, outs, containers, mutables;
		ins = outs = containers = mutables = 1;
		for (FunctionalMethodType fmt : fmts) {
			switch (fmt.itemIO()) {
				case INPUT:
					fmtNames.add("input" + ins++);
					break;
				case OUTPUT:
					fmtNames.add("output" + outs++);
					break;
				case CONTAINER:
					fmtNames.add("container" + containers++);
					break;
				case MUTABLE:
					fmtNames.add("mutable" + mutables++);
					break;
				default:
					throw new RuntimeException("Unexpected ItemIO type encountered!");
			}
		}
		return fmtNames;
	}

	@Override
	public List<SynthesizedParameterMember<?>> synthesizeMembers(
		List<FunctionalMethodType> fmts)
	{
		Boolean[] optionality = getParameterNullability(m, opType, m
			.getParameterCount());
		List<String> names = getParameterNames(fmts);
		int p = 0;
		List<SynthesizedParameterMember<?>> members = new ArrayList<>(fmts.size());
		for (FunctionalMethodType fmt : fmts) {
			String name = names.get(p);
			boolean optional = fmt.itemIO() != ItemIO.OUTPUT && optionality[p++];
			members.add(new SynthesizedParameterMember<>(fmt, name, !optional, ""));
		}
		return members;
	}

	private static Boolean[] getParameterNullability(Method m, Class<?> opType,
		int opParams)
	{
		boolean opMethodHasNullables = FunctionalParameters.hasNullableAnnotations(
			m);
		List<Method> fMethodsWithNullables = FunctionalParameters
			.fMethodsWithNullable(opType);
		if (opMethodHasNullables) {
			fMethodsWithNullables.add(m);
		}

		// Ensure only the Op method OR ONE of its op type's functional methods have
		// Nullables
		if (fMethodsWithNullables.size() > 1) {
			throw new NullablesOnMultipleMethodsException(m, fMethodsWithNullables);
		}

		// return the nullability of each parameter of the Op
		if (opMethodHasNullables) return getOpMethodNullables(m, opParams);
		if (!fMethodsWithNullables.isEmpty()) return FunctionalParameters
			.findParameterNullability(fMethodsWithNullables.get(0));
		return FunctionalParameters.generateAllRequiredArray(opParams);
	}

	private static Boolean[] getOpMethodNullables(Method m, int opParams) {
		int[] paramIndex = mapFunctionalParamsToIndices(m.getParameters());
		Boolean[] arr = FunctionalParameters.generateAllRequiredArray(opParams);
		// check parameters on m
		Boolean[] mNullables = FunctionalParameters.findParameterNullability(m);
		for (int i = 0; i < mNullables.length; i++) {
			int index = paramIndex[i];
			if (index == -1) continue;
			arr[index] |= mNullables[i];
		}
		return arr;
	}

	/**
	 * Since Ops written as methods can have an {@link OpDependency} (or multiple)
	 * as parameters, we need to determine which parameter indices correspond to
	 * the inputs of the Op.
	 *
	 * @param parameters the list of {@link Parameter}s of the Op
	 * @return an array of ints where the value at index {@code i} denotes the
	 *         position of the parameter in the Op's signature. Values of
	 *         {@code -1} designate an {@link OpDependency} at that position.
	 */
	private static int[] mapFunctionalParamsToIndices(Parameter[] parameters) {
		int[] paramNo = new int[parameters.length];
		int paramIndex = 0;
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].isAnnotationPresent(OpDependency.class)) {
				paramNo[i] = -1;
			}
			else {
				paramNo[i] = paramIndex++;
			}
		}
		return paramNo;
	}

}
