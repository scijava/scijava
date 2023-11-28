/*-
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

package org.scijava.ops.engine.struct;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.scijava.function.Producer;
import org.scijava.ops.api.Ops;
import org.scijava.ops.engine.exceptions.impl.NullablesOnMultipleMethodsException;
import org.scijava.ops.spi.OpDependency;
import org.scijava.struct.FunctionalMethodType;

/**
 * Lazily generates the parameter data for a {@link List} of
 * {@link FunctionalMethodType}s. <b>If</b> there exists a <b>full</b> set of
 * {@code @param} and {@code @return} tags, the javadoc will be used to create
 * the parameter names and descriptions. Otherwise, reasonable defaults will be
 * used.
 * 
 * @author Gabriel Selzer
 */
public class LazilyGeneratedMethodParameterData implements ParameterData {

	private static final Map<Method, MethodParamInfo> paramDataMap =
		new HashMap<>();

	private final Method m;
	private final Class<?> opType;

	public LazilyGeneratedMethodParameterData(Method m, Class<?> opType) {
		this.m = m;
		this.opType = opType;
	}

	public static MethodParamInfo getInfo(List<FunctionalMethodType> fmts,
		Method m, Class<?> opType)
	{
		if (!paramDataMap.containsKey(m)) generateMethodParamInfo(fmts, m, opType);
		MethodParamInfo info = paramDataMap.get(m);
		if (!info.containsAll(fmts)) updateMethodParamInfo(info, fmts, m, opType);
		return info;
	}

	private static void updateMethodParamInfo(MethodParamInfo info,
		List<FunctionalMethodType> fmts, Method m, Class<?> opType)
	{
		Map<FunctionalMethodType, String> fmtNames = info.getFmtNames();
		Map<FunctionalMethodType, String> fmtDescriptions = info
			.getFmtDescriptions();
		Map<FunctionalMethodType, Boolean> fmtNullability = info
			.getFmtNullability();
		// determine the Op inputs/outputs
		long numOpParams = m.getParameterCount();
		long numReturns = m.getReturnType() == void.class ? 0 : 1;
		Boolean[] paramNullability = getParameterNullability(m, opType,
			(int) numOpParams);

		addSynthesizedMethodParamInfo(fmtNames, fmtDescriptions, fmts,
			fmtNullability, paramNullability);
	}

	public static synchronized void generateMethodParamInfo(
		List<FunctionalMethodType> fmts, Method m, Class<?> opType)
	{
		if (paramDataMap.containsKey(m)) return;

		// determine the Op inputs/outputs
		long numOpParams = m.getParameterCount();
		long numReturns = m.getReturnType() == void.class ? 0 : 1;

		opType = Ops.findFunctionalInterface(opType);
		Boolean[] paramNullability = getParameterNullability(m, opType,
			(int) numOpParams);

		paramDataMap.put(m, synthesizedMethodParamInfo(fmts, paramNullability));
	}

	private static void addSynthesizedMethodParamInfo(
		Map<FunctionalMethodType, String> fmtNames,
		Map<FunctionalMethodType, String> fmtDescriptions,
		List<FunctionalMethodType> fmts,
		Map<FunctionalMethodType, Boolean> fmtNullability,
		Boolean[] paramNullability)
	{
		int ins, outs, containers, mutables;
		ins = outs = containers = mutables = 1;
		int nullableIndex = 0;
		for (FunctionalMethodType fmt : fmts) {
			fmtDescriptions.put(fmt, "");
			switch (fmt.itemIO()) {
				case INPUT:
					fmtNames.put(fmt, "input" + ins++);
					fmtNullability.put(fmt, paramNullability[nullableIndex++]);
					break;
				case OUTPUT:
					fmtNames.put(fmt, "output" + outs++);
					break;
				case CONTAINER:
					fmtNames.put(fmt, "container" + containers++);
					break;
				case MUTABLE:
					fmtNames.put(fmt, "mutable" + mutables++);
					break;
				default:
					throw new RuntimeException("Unexpected ItemIO type encountered!");
			}
		}
	}

	private static MethodParamInfo synthesizedMethodParamInfo(
		List<FunctionalMethodType> fmts, Boolean[] paramNullability)
	{
		Map<FunctionalMethodType, String> fmtNames = new HashMap<>(fmts.size());
		Map<FunctionalMethodType, String> fmtDescriptions = new HashMap<>(fmts
			.size());
		Map<FunctionalMethodType, Boolean> fmtNullability = new HashMap<>(fmts
			.size());

		addSynthesizedMethodParamInfo(fmtNames, fmtDescriptions, fmts,
			fmtNullability, paramNullability);

		return new MethodParamInfo(fmtNames, fmtDescriptions, fmtNullability);
	}

	@Override
	public List<SynthesizedParameterMember<?>> synthesizeMembers(
		List<FunctionalMethodType> fmts)
	{
		Producer<MethodParamInfo> p = //
			() -> LazilyGeneratedMethodParameterData.getInfo(fmts, m, opType);

		return fmts.stream() //
			.map(fmt -> new SynthesizedParameterMember<>(fmt, p)) //
			.collect(Collectors.toList());
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
