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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.LongFunction;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.OpInfoGenerator;
import org.scijava.struct.Member;

public class ReducedOpInfoGenerator implements OpInfoGenerator {

	public static final List<InfoReducer> infoReducers = ServiceLoader.load(
		InfoReducer.class).stream().map(ServiceLoader.Provider::get).collect(
			Collectors.toList());

	@Override
	public boolean canGenerateFrom(Object o) {
		// We can only generate OpInfos from other OpInfos
		if (!(o instanceof OpInfo)) return false;
		OpInfo info = (OpInfo) o;
		// We only benefit from OpInfos with optional parameters
		boolean allParamsRequired = info.inputs().parallelStream() //
			.allMatch(Member::isRequired); //
		if (allParamsRequired) return false;
		// If we have a InfoReducer, then we can reduce
		return infoReducers //
			.parallelStream() //
			.anyMatch(reducer -> reducer.canReduce(info));
	}

	@Override
	public List<OpInfo> generateInfosFrom(Object o) {
		if (!(o instanceof OpInfo)) return Collections.emptyList();
		return reduce((OpInfo) o);
	}

	private List<OpInfo> reduce(OpInfo info) {
		// Find the correct InfoReducer
		Optional<? extends InfoReducer> optionalReducer = infoReducers //
			.parallelStream() //
			.filter(infoReducer -> infoReducer.canReduce(info)) //
			.findAny();
		if (optionalReducer.isEmpty()) return Collections.emptyList();
		// We can only reduce as many times as we have optional params
		int numReductions = (int) info.struct().members().parallelStream() //
			.filter(m -> !m.isRequired()) //
			.count(); //
		// add a ReducedOpInfo for all possible reductions
		InfoReducer reducer = optionalReducer.get();
		LongFunction<OpInfo> func = l -> reducer.reduce(info, (int) l);
		return LongStream.range(1, numReductions + 1) //
			.mapToObj(func) //
			.filter(Objects::nonNull) //
			.collect(Collectors.toList());
	}

}
