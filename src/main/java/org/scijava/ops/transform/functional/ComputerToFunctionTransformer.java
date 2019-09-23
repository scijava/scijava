/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2018 SciJava developers.
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

package org.scijava.ops.transform.functional;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import org.scijava.ops.OpService;
import org.scijava.ops.OpUtils;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Functions;
import org.scijava.ops.function.Producer;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpTransformationException;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.ops.transform.TypeModUtils;
import org.scijava.ops.util.Adapt;
import org.scijava.param.ParameterStructs;
import org.scijava.plugin.Plugin;
import org.scijava.util.Types;

/**
 * Transforms computers into functions using the corresponding adapters in
 * {@link org.scijava.ops.util.Adapt.ComputerAdapt}.
 *
 * @author David Kolb
 * @author Marcel Wiedenmann
 */
@Plugin(type = OpTransformer.class)
public class ComputerToFunctionTransformer implements FunctionalTypeTransformer {

	private static final String CREATE_OP_NAME = "create";

	@Override
	public Object transform(final OpService opService, final Object src, final OpRef targetRef)
		throws OpTransformationException
	{
		final Class<?> targetFunctionalRawType = OpUtils.findFirstImplementedFunctionalInterface(targetRef);
		checkCanTransform(src, targetRef, targetFunctionalRawType);
		// It is almost always the case with Ops that the output is of the same typing
		// as the input (more specifically, the first input). Let's take a look for one
		// of those.
		final Type[] targetInputParamTypes = targetRef.getArgs();
		final Type targetOutputParamType = targetRef.getOutType();
		// NB: Resort to ordinary create Op if no input-aware one is available. Fail
		// if none of them is available.
		Function<?, ?> inputAwareCreate = null;
		Producer<?> create = null;
		try {
			final Type firstInput = targetInputParamTypes[0];
			if(Types.isApplicable(new Type[] {firstInput}, new Type[] {targetOutputParamType}) != -1) throw new IllegalArgumentException();
			inputAwareCreate = (Function<?, ?>) findInputAwareCreate(opService, Function.class, new Type[] {firstInput},
				targetOutputParamType);
		}
		catch (final IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
			try {
				create = findCreate(opService, targetOutputParamType);
			} catch (final IllegalArgumentException ex1) {
				ex1.addSuppressed(ex);
				throw createCannotTransformException(src, targetRef,
						"No suitable create Op available to create output parameter of source computer.", ex1);
			}
		}
		if (src instanceof Computers.Arity1) return computerToFunction((Computers.Arity1<?, ?>) src, inputAwareCreate, create);
		if (src instanceof Computers.Arity2) return computerToFunction((Computers.Arity2<?, ?, ?>) src, inputAwareCreate, create);
		if (src instanceof Computers.Arity3) return computerToFunction((Computers.Arity3<?, ?, ?, ?>) src, inputAwareCreate, create);
		throw createCannotTransformException(src, targetRef, "Source does not implement a supported computer interface.",
			null);
	}

	private void checkCanTransform(final Object src, final OpRef targetRef, final Class<?> targetFunctionalRawType)
		throws OpTransformationException
	{
		String problem = null;
		final Class<?> srcFunctionalRawType = ParameterStructs.findFunctionalInterface(src.getClass());
		if (srcFunctionalRawType == null) {
			problem = "Source does not implement a functional interface.";
		}
		else if (targetFunctionalRawType == null) {
			problem = "Target does not implement a functional interface.";
		}
		else {
			final Integer srcArity = Computers.ALL_COMPUTERS.get(srcFunctionalRawType);
			if (srcArity == null) {
				problem = "Source does not implement a known computer interface.";
			}
			else {
				final Integer targetArity = Functions.ALL_FUNCTIONS.get(targetFunctionalRawType);
				if (targetArity == null) {
					problem = "Target does not implement a known function interface.";
				}
				else if (!srcArity.equals(targetArity)) {
					problem = "Source and target arities disagree (" + srcArity + " vs. " + targetArity + ").";
				}
			}
		}
		if (problem != null) {
			throw createCannotTransformException(src, targetRef, problem, null);
		}
	}

	private static Object findInputAwareCreate(final OpService ops, final Class<?> createOpRawType,
		final Type[] inputParamTypes, final Type outputParamType)
	{
		final Type[] paramTypes = Stream.concat(Stream.of(inputParamTypes), Stream.of(outputParamType)).toArray(
			Type[]::new);
		final Type createOpType = Types.parameterize(createOpRawType, paramTypes);
		final OpRef opRef = OpRef.fromTypes(CREATE_OP_NAME, new Type[] { createOpType }, outputParamType, inputParamTypes);
		return ops.findOpInstance(CREATE_OP_NAME, opRef);
	}

	private static Producer<?> findCreate(final OpService ops, final Type outputParamType) {
		final Type createOpType = Types.parameterize(Producer.class, new Type[] { outputParamType });
		final OpRef opRef = OpRef.fromTypes(CREATE_OP_NAME, new Type[] { createOpType }, outputParamType);
		return (Producer<?>) ops.findOpInstance(CREATE_OP_NAME, opRef);
	}

	private static <I, O> Function<I, O> computerToFunction(final Computers.Arity1<I, O> src, final Object inputAwareCreate,
		final Producer<?> create)
	{
		if(inputAwareCreate != null) return Adapt.ComputerAdapt.asFunction(src, (Function<I, O>) inputAwareCreate);
		Function<I, O> inputAwareSource = (in) -> ((Producer<O>)create).create();
		return Adapt.ComputerAdapt.asFunction(src, inputAwareSource);
	}

	private static <I1, I2, O> BiFunction<I1, I2, O> computerToFunction(final Computers.Arity2<I1, I2, O> src,
		final Object inputAwareCreate, final Producer<?> create)
	{
		if(inputAwareCreate != null) return Adapt.ComputerAdapt.asBiFunction(src, (Function<I1, O>) inputAwareCreate);
		Function<I1, O> inputAwareSource = (in) -> ((Producer<O>)create).create();
		return Adapt.ComputerAdapt.asBiFunction(src, inputAwareSource);
	}

	private static <I1, I2, I3, O> Functions.Arity3<I1, I2, I3, O> computerToFunction(final Computers.Arity3<I1, I2, I3, O> src,
		final Object inputAwareCreate, final Producer<?> create)
	{
		if(inputAwareCreate != null) return Adapt.ComputerAdapt.asFunction3(src, (Function<I1, O>) inputAwareCreate);
		Function<I1, O> inputAwareSource = (in) -> ((Producer<O>)create).create();
		return Adapt.ComputerAdapt.asFunction3(src, inputAwareSource);
	}

	@Override
	public OpRef substituteAnyInTargetRef(OpRef srcRef, OpRef targetRef) {
		final Type[] targetTypes = srcRef.getTypes();
		TypeModUtils.replaceRawTypes(targetTypes, Types.raw(srcRef.getTypes()[0]), Types.raw(targetRef.getTypes()[0]));
		return new OpRef(targetRef.getName(), targetTypes, srcRef.getOutType(), targetRef.getArgs());
	}
	
	@Override
	public Integer getTargetArity(final Class<?> targetFunctionalRawType) {
		return Functions.ALL_FUNCTIONS.get(targetFunctionalRawType);
	}

	@Override
	public List<Class<?>> getSourceFunctionalInterfaces(final int targetArity) {
		final Class<?> computer = Computers.ALL_COMPUTERS.inverse().get(targetArity);
		return computer != null ? Collections.singletonList(computer) : Collections.emptyList();
	}

	@Override
	public Type[] getSourceInputParameterTypes(final OpRef targetRef, final int targetArity) {
		// NB: Output parameter is also part of input parameters in computers.
		return TypeModUtils.insert(targetRef.getArgs(), targetRef.getOutType(), targetArity);
	}
}
