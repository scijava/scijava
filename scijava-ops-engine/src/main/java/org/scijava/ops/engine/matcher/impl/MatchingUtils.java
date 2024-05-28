/*
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

package org.scijava.ops.engine.matcher.impl;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;

import org.scijava.types.Any;
import org.scijava.types.Types;
import org.scijava.types.Types.TypeVarInfo;

public final class MatchingUtils {

	private MatchingUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Checks for raw assignability.
	 * <p>
	 * TODO: This method is not yet fully implemented.
	 *  The correct behavior should be as follows.
	 *  Suppose we have a generic typed method like:
	 * </p>
	 *
	 * <pre>
	 * public static &lt;N&gt; List&lt;N&gt; foo(N in) {
	 *   ...
	 * }
	 * </pre>
	 *
	 * This method should discern if the following assignments would be legal,
	 * possibly using predetermined {@link TypeVariable} assignments:
	 *
	 * <pre>
	 * List&lt;Integer&gt; listOfInts = foo(new Integer(0)) //legal
	 * List&lt;Number&gt; listOfNumbers = foo(new Integer(0)) //legal
	 * List&lt;? extends Number&gt; listOfBoundedWildcards = foo(new Integer(0)) //legal
	 * </pre>
	 *
	 * The corresponding calls to this method would be:
	 *
	 * <pre>
	 * Nil&lt;List&lt;N&gt;&gt; nilN = new Nil&lt;List&lt;N&gt;&gt;(){}
	 * Nil&lt;List&lt;Integer&gt;&gt; nilInteger = new Nil&lt;List&lt;Integer&gt;&gt;(){}
	 * Nil&lt;List&lt;Number&gt;&gt; nilNumber = new Nil&lt;List&lt;Number&gt;&gt;(){}
	 * Nil&lt;List&lt;? extends Number&gt;&gt; nilWildcardNumber = new Nil&lt;List&lt;? extends Number&gt;&gt;(){}
	 *
	 * checkGenericOutputsAssignability(nilN.type(), nilInteger.type(), ...)
	 * checkGenericOutputsAssignability(nilN.type(), nilNumber.type(), ...)
	 * checkGenericOutputsAssignability(nilN.type(), nilWildcardNumber.type(), ...)
	 * </pre>
	 *
	 * Using a map where N was already bound to Integer (N -> Integer.class). This
	 * method is useful for the following scenario: During ops matching, we first
	 * check if the arguments (inputs) of the requested op are applicable to the
	 * arguments of an Op candidate. During this process, possible type variables
	 * may be inferred. The can then be used with this method to find out if the
	 * outputs of the op candidate would be assignable to the output of the
	 * requested op.
	 *
	 * @param froms
	 * @param tos
	 * @param typeBounds
	 * @return the index {@code i} such that {@code from[i]} cannot be assigned to
	 *         {@code to[i]}, or {@code -1} iff {@code from[i]} can be assigned to
	 *         {@code to[i]} for all {@code 0 <= i < from.length}.
	 */
	static int checkGenericOutputsAssignability(Type[] froms, Type[] tos,
		HashMap<TypeVariable<?>, TypeVarInfo> typeBounds)
	{
		for (int i = 0; i < froms.length; i++) {
			Type from = froms[i];
			Type to = tos[i];

			if (Any.is(to)) continue;

			if (from instanceof TypeVariable) {
				TypeVarInfo typeVarInfo = typeBounds.get(from);
				// HACK: we CAN assign, for example, a Function<Iterable<N>, O> to a
				// Function<Iterable<Integer>, Double>, because in this situation O is
				// not bounded to any other types. However isAssignable will fail,
				// since we cannot just cast Double to O without that required
				// knowledge that O can be fixed to Double. We get around this by
				// recording in `typeBounds` that our previously unbounded TypeVariable
				// `from` is now fixed to `to`, then simply assigning `from` to `to`,
				// since `from` only has one bound, being `to`.
				if (typeVarInfo == null) {
					TypeVariable<?> fromTypeVar = (TypeVariable<?>) from;
					TypeVarInfo fromInfo = new TypeVarInfo(fromTypeVar);
					fromInfo.fixBounds(to, true);
					typeBounds.put(fromTypeVar, fromInfo);
					from = to;
				}
				// similar to the above, if we know that O is already bound to a Type,
				// and that Type is to, then we can assign this without any issues.
				else {
					if (typeVarInfo.allowType(to, true)) from = to;
				}
			}

			if (!Types.isAssignable(Types.raw(from), Types.raw(to))) return i;
		}
		return -1;
	}
}
