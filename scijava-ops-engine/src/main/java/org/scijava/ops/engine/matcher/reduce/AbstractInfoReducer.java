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

package org.scijava.ops.engine.matcher.reduce;

import java.lang.reflect.Type;

import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.types.Types;
import org.scijava.types.infer.FunctionalInterfaces;

public abstract class AbstractInfoReducer implements InfoReducer {

	@Override
	public boolean canReduce(OpInfo info) {
		boolean isReducerType = isReducerType(FunctionalInterfaces.findFrom(info
			.opType()));
		boolean canReduce = info.declaredHints().containsNone(
			BaseOpHints.Reduction.FORBIDDEN);
		return isReducerType && canReduce;
	}

	@Override
	public ReducedOpInfo reduce(OpInfo info, int numReductions) {
		Class<?> rawType = FunctionalInterfaces.findFrom(info.opType());
		int originalArity = arityOf(rawType);
		int reducedArity = originalArity - numReductions;
		Class<?> reducedRawType = ofArity(reducedArity);
		Type[] inputTypes = info.inputTypes().toArray(Type[]::new);
		Type outputType = info.output().type();
		Type[] newTypes = new Type[reducedArity + 1];
		if (reducedArity >= 0) System.arraycopy(inputTypes, 0, newTypes, 0,
			reducedArity);
		newTypes[newTypes.length - 1] = outputType;
		Type reducedOpType = Types.parameterize(reducedRawType, newTypes);
		return new ReducedOpInfo(info, reducedOpType, originalArity - reducedArity);
	}

	protected abstract boolean isReducerType(Class<?> functionalInterface);

	protected abstract int arityOf(Class<?> rawType);

	protected abstract Class<?> ofArity(int reducedArity);
}
