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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.scijava.ops.engine.exceptions.impl.NullablesOnMultipleMethodsException;
import org.scijava.struct.ItemIO;
import org.scijava.types.inference.FunctionalInterfaces;

/**
 * Lazily generates the parameter data for a {@link List} of
 * {@link FunctionalMethodType}s. <b>If</b> there exists a <b>full</b> set of
 * {@code @param} and {@code @return} tags, the javadoc will be used to create
 * the parameter names and descriptions. Otherwise, reasonable defaults will be
 * used.
 *
 * @author Gabriel Selzer
 */
public class SynthesizedFieldParameterData implements ParameterData {

	private final FieldInstance fieldInstance;
	private final int numInputs;

	public SynthesizedFieldParameterData(FieldInstance fieldInstance) {
		this.fieldInstance = fieldInstance;
		this.numInputs = FunctionalInterfaces //
			.functionalMethodOf(fieldInstance.field().getType()).getParameterCount();
	}

	@Override
	public List<SynthesizedParameterMember<?>> synthesizeMembers(
		List<FunctionalMethodType> fmts)
	{

		// determine the optionality of each input
		Boolean[] optionality = getParameterNullability(fieldInstance.instance(),
			fieldInstance.field(), numInputs);
		// determine the name of each fmt
		List<String> names = getParameterNames(fmts);
		// map fmts to members
		int p = 0;
		List<SynthesizedParameterMember<?>> members = new ArrayList<>(fmts.size());
		for (FunctionalMethodType fmt : fmts) {
			String name = names.get(p);
			boolean optional = fmt.itemIO() != ItemIO.OUTPUT && optionality[p++];
			members.add(new SynthesizedParameterMember<>(fmt, name, !optional, ""));
		}
		return members;
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

	private static Boolean[] getParameterNullability(Object instance, Field field,
		int opParams)
	{

		Class<?> fieldClass;
		try {
			fieldClass = field.get(instance).getClass();
		}
		catch (IllegalArgumentException | IllegalAccessException exc) {
			// TODO Auto-generated catch block
			throw new IllegalArgumentException(exc);
		}
		List<Method> fMethodsWithNullables = FunctionalParameters
			.fMethodsWithNullable(fieldClass);
		Class<?> fIface = FunctionalInterfaces.findFrom(fieldClass);
		List<Method> fIfaceMethodsWithNullables = FunctionalParameters
			.fMethodsWithNullable(fIface);

		if (fMethodsWithNullables.isEmpty() && fIfaceMethodsWithNullables
			.isEmpty())
		{
			return FunctionalParameters.generateAllRequiredArray(opParams);
		}
		if (!fMethodsWithNullables.isEmpty() && !fIfaceMethodsWithNullables
			.isEmpty())
		{
			List<Method> nullables = new ArrayList<>(fMethodsWithNullables);
			nullables.addAll(fIfaceMethodsWithNullables);
			throw new NullablesOnMultipleMethodsException(field, nullables);
		}
		if (fMethodsWithNullables.isEmpty()) {
			return FunctionalParameters.findParameterNullability(
				fIfaceMethodsWithNullables.get(0));
		}
		if (fIfaceMethodsWithNullables.isEmpty()) {
			return FunctionalParameters.findParameterNullability(fMethodsWithNullables
				.get(0));
		}
		return FunctionalParameters.generateAllRequiredArray(opParams);
	}

}
