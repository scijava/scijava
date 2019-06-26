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

import org.scijava.ops.OpService;
import org.scijava.ops.OpUtils;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpTransformationException;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.ops.transform.TypeModUtils;
import org.scijava.ops.types.Nil;
import org.scijava.ops.util.Adapt;
import org.scijava.ops.util.Computers;
import org.scijava.ops.util.Functions;
import org.scijava.param.ParameterStructs;
import org.scijava.plugin.Plugin;

/**
 * Transforms functions into computers using the corresponding adapters in
 * {@link org.scijava.ops.util.Adapt.Functions}.
 *
 * @author David Kolb
 * @author Marcel Wiedenmann
 */
@Plugin(type = OpTransformer.class)
public class FunctionToComputerTransformer implements FunctionalTypeTransformer {

	private static final String COPY_OP_NAME = "copy";

	@Override
	public Object transform(final OpService opService, final Object src, final OpRef targetRef)
		throws OpTransformationException
	{
		final Class<?> targetFunctionalRawType = OpUtils.findFirstImplementedFunctionalInterface(targetRef);
		checkCanTransform(src, targetRef, targetFunctionalRawType);
		final Type targetOutputParamType = targetRef.getOutType();
		Computer<?, ?> copy;
		try {
			copy = findCopy(opService, targetOutputParamType);
		}
		catch (final IllegalArgumentException ex) {
			throw createCannotTransformException(src, targetRef,
				"No suitable copy Op available to copy function output to computer output.", ex);
		}
		if (src instanceof Function) return functionToComputer((Function<?, ?>) src, copy);
		if (src instanceof BiFunction) return functionToComputer((BiFunction<?, ?, ?>) src, copy);
		throw createCannotTransformException(src, targetRef, "Source does not implement a supported function interface.",
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
			final Integer srcArity = Functions.ALL_FUNCTIONS.get(srcFunctionalRawType);
			if (srcArity == null) {
				problem = "Source does not implement a known function interface.";
			}
			else {
				final Integer targetArity = Computers.ALL_COMPUTERS.get(targetFunctionalRawType);
				if (targetArity == null) {
					problem = "Target does not implement a known computer interface.";
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

	private static Computer<?, ?> findCopy(final OpService opService, final Type outputParamType) {
		return Computers.unary(opService, COPY_OP_NAME, Nil.of(outputParamType), Nil.of(outputParamType));
	}

	private static <I, O> Computer<I, O> functionToComputer(final Function<I, O> src, final Computer<?, ?> copy) {
		return Adapt.Functions.asComputer(src, (Computer<O, O>) copy);
	}

	private static <I1, I2, O> BiComputer<I1, I2, O> functionToComputer(final BiFunction<I1, I2, O> src,
		final Computer<?, ?> copy)
	{
		return Adapt.Functions.asBiComputer(src, (Computer<O, O>) copy);
	}

	@Override
	public OpRef substituteAnyInTargetRef(OpRef srcRef, OpRef targetRef) {
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	@Override
	public Integer getTargetArity(final Class<?> targetFunctionalRawType) {
		return Computers.ALL_COMPUTERS.get(targetFunctionalRawType);
	}

	@Override
	public List<Class<?>> getSourceFunctionalInterfaces(final int targetArity) {
		final Class<?> function = Functions.ALL_FUNCTIONS.inverse().get(targetArity);
		return function != null ? Collections.singletonList(function) : Collections.emptyList();
	}

	@Override
	public Type[] getSourceInputParameterTypes(final OpRef targetRef, final int targetArity) {
		// NB: Output parameter is not part of input parameters in functions.
		return TypeModUtils.remove(targetRef.getArgs(), targetArity);
	}
}
