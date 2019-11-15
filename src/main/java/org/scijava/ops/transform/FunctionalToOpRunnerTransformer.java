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

package org.scijava.ops.transform;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.scijava.ops.OpService;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Functions;
import org.scijava.ops.function.Inplaces;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.util.OpRunners;
import org.scijava.param.ParameterStructs;
import org.scijava.plugin.Plugin;
import org.scijava.util.Types;

/**
 * @author David Kolb
 * @author Marcel Wiedenmann
 */
@Plugin(type = OpTransformer.class)
public class FunctionalToOpRunnerTransformer implements OpTransformer {

	@Override
	public OpRunner transform(final OpService opService, final Object src, final OpRef targetRef)
		throws OpTransformationException
	{
		final Class<?> srcFunctionalRawType = ParameterStructs.findFunctionalInterface(src.getClass());
		if (srcFunctionalRawType != null) {
			if (Computers.isComputer(srcFunctionalRawType)) {
				// NB: Increase arity by one to account for Op runner's additional
				// output parameter. See below for more details.
				checkCanTransform(src, Computers.ALL_COMPUTERS.get(srcFunctionalRawType) + 1, targetRef);
				return computerToRunner(src, srcFunctionalRawType);
			}
			if (Functions.isFunction(srcFunctionalRawType)) {
				checkCanTransform(src, Functions.ALL_FUNCTIONS.get(srcFunctionalRawType), targetRef);
				return functionToRunner(src, srcFunctionalRawType);
			}
			if (Inplaces.isInplace(srcFunctionalRawType)) {
				checkCanTransform(src, Inplaces.ALL_INPLACES.get(srcFunctionalRawType).arity(), targetRef);
				return inplaceToRunner(src, srcFunctionalRawType);
			}
		}
		throw createInvalidSourceOpException(src, "does not implement a functional interface.");
	}

	private static void checkCanTransform(final Object src, final int srcArity, final OpRef targetRef)
		throws OpTransformationException
	{
		String problem = null;
		if (!isOpRunner(targetRef)) {
			problem = "Target is not an " + OpRunner.class.getName() + ".";
		}
		else {
			final int targetArity = targetRef.getArgs().length;
			if (srcArity != targetArity) {
				problem = "Source and target arities disagree (" + srcArity + " vs. " + targetArity + ").";
			}
		}
		if (problem != null) {
			throw new OpTransformationException("Cannot transform source Op:\n" + src.getClass().getName() +
				"into target:\n" + targetRef + "\nusing transformer: " + FunctionalToOpRunnerTransformer.class.getName() +
				".\n" + problem);
		}
	}

	private static boolean isOpRunner(final OpRef targetRef) {
		return Arrays.stream(targetRef.getTypes()) //
			.anyMatch(t -> OpRunner.class.isAssignableFrom(Types.raw(t)));
	}

	private static OpRunner computerToRunner(final Object src, final Class<?> srcFunctionalRawType)
		throws OpTransformationException
	{
		if (src instanceof Computers.Arity0) return OpRunners.ComputerRunner.toRunner((Computers.Arity0<?>) src); 
		if (src instanceof Computers.Arity1) return OpRunners.ComputerRunner.toRunner((Computers.Arity1<?, ?>) src);
		if (src instanceof Computers.Arity2) return OpRunners.ComputerRunner.toRunner((Computers.Arity2<?, ?, ?>) src);
		if (src instanceof Computers.Arity3) return OpRunners.ComputerRunner.toRunner((Computers.Arity3<?, ?, ?, ?>) src);
		throw createInvalidSourceOpException(src,
			"could not be transformed. The implemented computer type (%s) is not supported by this transformer.",
			srcFunctionalRawType.getName());
	}

	private static OpRunner functionToRunner(final Object src, final Class<?> srcFunctionalRawType)
		throws OpTransformationException
	{
		if (src instanceof Supplier) return OpRunners.FunctionRunner.toRunner((Supplier<?>) src);
		if (src instanceof Function) return OpRunners.FunctionRunner.toRunner((Function<?, ?>) src);
		if (src instanceof BiFunction) return OpRunners.FunctionRunner.toRunner((BiFunction<?, ?, ?>) src);
		if (src instanceof Functions.Arity3) return OpRunners.FunctionRunner.toRunner((Functions.Arity3<?, ?, ?, ?>) src);
		throw createInvalidSourceOpException(src,
			"could not be transformed. The implemented function type (%s) is not supported by this transformer.",
			srcFunctionalRawType.getName());
	}

	private static OpRunner inplaceToRunner(final Object src, final Class<?> srcFunctionalRawType)
		throws OpTransformationException
	{
		if (src instanceof Inplaces.Arity1) return OpRunners.InplaceRunner.toRunner((Inplaces.Arity1<?>) src);
		if (src instanceof Inplaces.Arity2_1) return OpRunners.InplaceRunner.toRunner((Inplaces.Arity2_1<?, ?>) src);
		if (src instanceof Inplaces.Arity2_2) return OpRunners.InplaceRunner.toRunner((Inplaces.Arity2_2<?, ?>) src);
		if (src instanceof Inplaces.Arity3_1) return OpRunners.InplaceRunner.toRunner((Inplaces.Arity3_1<?, ?, ?>) src);
		if (src instanceof Inplaces.Arity3_2) return OpRunners.InplaceRunner.toRunner((Inplaces.Arity3_2<?, ?, ?>) src);
		if (src instanceof Inplaces.Arity3_3) return OpRunners.InplaceRunner.toRunner((Inplaces.Arity3_3<?, ?, ?>) src);
		throw createInvalidSourceOpException(src,
			"could not be transformed. The implemented inplace type (%s) is not supported by this transformer.",
			srcFunctionalRawType.getName());
	}

	private static OpTransformationException createInvalidSourceOpException(final Object src, final String problem,
		final Object... problemArgs)
	{
		return new OpTransformationException("Source Op:\n" + src.getClass().getName() + String.format(problem,
			problemArgs));
	}

	@Override
	public Collection<OpRef> getRefsTransformingTo(final OpRef targetRef) {
		if (!isOpRunner(targetRef)) {
			return Collections.emptyList();
		}
		final Type[] targetInputParamTypes = targetRef.getArgs();

		final int targetArity = targetInputParamTypes.length;
		// NB: Decrease arity by one when looking for matching computers since their
		// output parameter is part of the Op runner's list of input parameters. The
		// Op runner's (additional) output parameter is always of type Object here.
		// Note that we don't need to do this for inplaces because their arity is
		// counted differently and already includes the output (= mutated input)
		// parameter.
		final Class<?> srcComputer = getSourceComputerOfArity(targetArity - 1);
		final Class<?> srcFunction = getSourceFunctionOfArity(targetArity);
		final List<Class<?>> srcInplaces = getInplacesOfArity(targetArity);

		final List<OpRef> srcRefs = new ArrayList<>(1 + 1 + srcInplaces.size());
		addFunctionSourceRef(srcRefs, srcFunction, targetRef, targetInputParamTypes);
		addComputerAndInplacesSourceRefs(srcRefs, srcComputer, srcInplaces, targetRef, targetInputParamTypes);
		return srcRefs;
	}

	private static Class<?> getSourceComputerOfArity(final int arity) {
		return Computers.ALL_COMPUTERS.inverse().get(arity);
	}

	private static Class<?> getSourceFunctionOfArity(final int arity) {
		return Functions.ALL_FUNCTIONS.inverse().get(arity);
	}

	private static List<Class<?>> getInplacesOfArity(final int arity) {
		return Inplaces.getInplacesOfArity(arity);
	}

	private static void addFunctionSourceRef(final List<OpRef> srcRefs, final Class<?> srcFunction, final OpRef targetRef,
		final Type[] targetInputParamTypes)
	{
		if (srcFunction != null) {
			// NB: Use both input and output parameters of the Op runner.
			final Type[] targetParamTypes = Stream.concat(Arrays.stream(targetInputParamTypes), Stream.of(targetRef
				.getOutType())).toArray(Type[]::new);
			final Type[] targetOpTypes = parameterizeTargetOpTypes(targetRef, targetParamTypes);
			addSourceRef(srcRefs, srcFunction, targetRef, targetOpTypes, targetInputParamTypes);
		}
	}

	private static void addComputerAndInplacesSourceRefs(final List<OpRef> srcRefs, final Class<?> srcComputer,
		final List<Class<?>> srcInplaces, final OpRef targetRef, final Type[] targetInputParamTypes)
	{
		// NB: Use only the input parameters of the Op runner.
		final Type[] targetParamTypes = targetInputParamTypes.clone();
		final Type[] targetOpTypes = parameterizeTargetOpTypes(targetRef, targetParamTypes);
		if (srcComputer != null) addSourceRef(srcRefs, srcComputer, targetRef, targetOpTypes, targetInputParamTypes);
//		if (srcComputer != null) addComputerSourceRef(srcRefs, srcComputer, targetRef, targetOpTypes, targetInputParamTypes);
		for (final Class<?> srcInplace : srcInplaces) {
			addSourceRef(srcRefs, srcInplace, targetRef, targetOpTypes, targetInputParamTypes);
		}
	}

	private static Type[] parameterizeTargetOpTypes(final OpRef targetRef, final Type[] targetParamTypes) {
		return Arrays.stream(targetRef.getTypes()).map(t -> Types.parameterize(Types.raw(t), targetParamTypes)).toArray(
			Type[]::new);
	}

	private static void addSourceRef(final List<OpRef> srcRefs, final Class<?> srcOpRawType, final OpRef targetRef,
		final Type[] targetOpTypes, final Type[] targetInputParamTypes)
	{
		final Type[] srcOpTypes = targetOpTypes.clone();
		final boolean hit = TypeModUtils.replaceRawTypes(srcOpTypes, OpRunner.class, srcOpRawType);
		if (hit) {
			srcRefs.add(OpRef.fromTypes(targetRef.getName(), srcOpTypes, targetRef.getOutType(), targetInputParamTypes));
		}
	}

	private static void addComputerSourceRef(final List<OpRef> srcRefs, final Class<?> srcOpRawType, final OpRef targetRef,
		final Type[] targetOpTypes, final Type[] targetInputParamTypes)
	{
		final Type[] srcOpTypes = targetOpTypes.clone();
		final boolean hit = TypeModUtils.replaceRawTypes(srcOpTypes, OpRunner.class, srcOpRawType);
		if (hit) {
			srcRefs.add(OpRef.fromTypes(targetRef.getName(), srcOpTypes, targetInputParamTypes[targetInputParamTypes.length - 1], targetInputParamTypes));
		}
	}
}
