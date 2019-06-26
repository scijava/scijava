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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.scijava.ops.OpService;
import org.scijava.ops.OpUtils;
import org.scijava.ops.core.inplace.BiInplaceFirst;
import org.scijava.ops.core.inplace.BiInplaceSecond;
import org.scijava.ops.core.inplace.Inplace;
import org.scijava.ops.core.inplace.Inplace3First;
import org.scijava.ops.core.inplace.Inplace3Second;
import org.scijava.ops.core.inplace.Inplace4First;
import org.scijava.ops.core.inplace.Inplace5First;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpTransformationException;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.ops.util.Adapt;
import org.scijava.ops.util.Functions;
import org.scijava.ops.util.Inplaces;
import org.scijava.ops.util.Inplaces.InplaceInfo;
import org.scijava.param.ParameterStructs;
import org.scijava.plugin.Plugin;
import org.scijava.util.Types;

/**
 * Transforms inplaces into functions using the corresponding adapters in
 * {@link org.scijava.ops.util.Adapt.Inplaces}.
 *
 * @author Marcel Wiedenmann
 */
@Plugin(type = OpTransformer.class)
public class InplaceToFunctionTransformer implements FunctionalTypeTransformer {

	@Override
	public Object transform(final OpService opService, final Object src, final OpRef targetRef)
		throws OpTransformationException
	{
		final Class<?> targetFunctionalRawType = OpUtils.findFirstImplementedFunctionalInterface(targetRef);
		checkCanTransform(src, targetRef, targetFunctionalRawType);
		if (src instanceof Inplace) return Adapt.Inplaces.asFunction((Inplace<?>) src);
		if (src instanceof BiInplaceFirst) return Adapt.Inplaces.asBiFunction((BiInplaceFirst<?, ?>) src);
		if (src instanceof BiInplaceSecond) return Adapt.Inplaces.asBiFunction((BiInplaceSecond<?, ?>) src);
		if (src instanceof Inplace3First) return Adapt.Inplaces.asFunction3((Inplace3First<?, ?, ?>) src);
		if (src instanceof Inplace3Second) return Adapt.Inplaces.asFunction3((Inplace3Second<?, ?, ?>) src);
		if (src instanceof Inplace4First) return Adapt.Inplaces.asFunction4((Inplace4First<?, ?, ?, ?>) src);
		if (src instanceof Inplace5First) return Adapt.Inplaces.asFunction5((Inplace5First<?, ?, ?, ?, ?>) src);
		throw createCannotTransformException(src, targetRef, "Source does not implement a supported inplace interface.",
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
			final InplaceInfo srcInfo = Inplaces.ALL_INPLACES.get(srcFunctionalRawType);
			if (srcInfo == null) {
				problem = "Source does not implement a known inplace interface.";
			}
			else {
				final Integer targetArity = Functions.ALL_FUNCTIONS.get(targetFunctionalRawType);
				if (targetArity == null) {
					problem = "Target does not implement a known function interface.";
				}
				else {
					final int srcArity = srcInfo.arity();
					if (srcArity != targetArity.intValue()) {
						problem = "Source and target arities disagree (" + srcArity + " vs. " + targetArity + ").";
					}
				}
			}
		}
		if (problem != null) {
			throw createCannotTransformException(src, targetRef, problem, null);
		}
	}

	@Override
	public OpRef substituteAnyInTargetRef(OpRef srcRef, OpRef targetRef) {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Integer getTargetArity(final Class<?> targetFunctionalRawType) {
		return Functions.ALL_FUNCTIONS.get(targetFunctionalRawType);
	}

	@Override
	public List<Class<?>> getSourceFunctionalInterfaces(final int targetArity) {
		return Inplaces.getInplacesOfArity(targetArity);
	}

	@Override
	public Type getSourceOpType(final Type targetOpType, final Class<?> targetFunctionalRawType,
		final Class<?> sourceFunctionalRawType)
	{
		if (targetOpType instanceof ParameterizedType) {
			final Type[] targetParamTypes = ((ParameterizedType) targetOpType).getActualTypeArguments();
			final int srcMutableParamPosition = Inplaces.ALL_INPLACES.get(sourceFunctionalRawType).mutablePosition();
			final int targetOutputParamPosition = targetParamTypes.length - 1;
			if (targetParamTypes[srcMutableParamPosition].equals(targetParamTypes[targetOutputParamPosition])) {
				// NB: Drop output parameter of the function as it's essentially the
				// mutable position of the inplace.
				return Types.parameterize(sourceFunctionalRawType, Arrays.copyOf(targetParamTypes, targetOutputParamPosition));
			}
		}
		return null;
	}

	@Override
	public Type[] getSourceInputParameterTypes(final OpRef targetRef, final int targetArity) {
		return targetRef.getArgs();
	}
}
