/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

import org.scijava.ops.matcher.OpRef;

/**
 * Utility class to do transformations on {@link OpRef}s.
 * 
 * @author David Kolb
 */
public final class OpRefTransformUtils {

	private OpRefTransformUtils() {
		// NB: prevent instantiation of utility class.
	}

	// -- Utility methods --

	/**
	 * Attempts to perform "unlift" transformation of the specified
	 * {@link OpRef} if it matches the specified classes. Otherwise null is
	 * returned, indicating that transformation is not possible. If no indices
	 * are given, all types will be unlifted.
	 * 
	 * @param toRef
	 *            the ref to transform
	 * @param rawSearchClass
	 *            the functional raw type to look for
	 * @param unliftRawClass
	 *            the raw type of types to unlift
	 * @param typeArgumentIndices
	 *            the indices of type arguments of the functional refTypes to unlift
	 * @param argIndices
	 *            the indices of arg types of the ref to unlift
	 * @param outputIndices
	 *            the indices of output types of the ref to unlift
	 * @return
	 */
	public static OpRef unliftTransform(OpRef toRef, Class<?> rawSearchClass, Class<?> unliftRawClass,
			Integer[] typeArgumentIndices, Integer[] argIndices, Integer[] outputIndices) {
		Type[] refTypes = toRef.getTypes();
		boolean typesChanged = TypeModUtils.unliftParameterizedTypes(refTypes, rawSearchClass, unliftRawClass,
				typeArgumentIndices);
		// TODO We assume here that the functional input is the first Type in
		// this list and all others are secondary args if there are any (we do
		// not want to touch them as they are not part of op transformations).
		// Should always be the case as during structification of the ops,
		// the functional args are always checked first. Hence, they always
		// need to be requested first and are thus at the beginning of the list.
		// From the functional type we know how many there must be.
		Type[] args = toRef.getArgs();
		boolean argsChanged = TypeModUtils.unliftTypes(args, unliftRawClass, argIndices);
		Type[] outs = new Type[] { toRef.getOutType() };
		boolean outsChanged = TypeModUtils.unliftTypes(outs, unliftRawClass, outputIndices);

		if (typesChanged && argsChanged && outsChanged) {
			return OpRef.fromTypes(toRef.getName(), refTypes, outs[0], args);
		}
		return null;
	}
}
