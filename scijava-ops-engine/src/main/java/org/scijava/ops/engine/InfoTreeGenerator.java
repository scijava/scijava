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

package org.scijava.ops.engine;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.scijava.ops.api.InfoTree;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.priority.Prioritized;
import org.scijava.priority.Priority;

public interface InfoTreeGenerator extends Prioritized<InfoTreeGenerator> {

	/**
	 * Generates an {@link InfoTree}. This {@link InfoTreeGenerator} is only
	 * responsible for generating the <b>outer layer</b> of the {@link InfoTree},
	 * and delegates to {@code generators} to generate other needed components,
	 * such as dependencies.
	 *
	 * @param env the {@link OpEnvironment}
	 * @param signature the signature for which an {@link InfoTreeGenerator} must
	 *          be generated
	 * @param idMap the available {@link OpInfo}s, keyed by their id
	 * @param generators the available {@link InfoTreeGenerator}s
	 * @return an {@link InfoTree} matching the specifications of
	 *         {@code signature}
	 */
	InfoTree generate(OpEnvironment env, String signature,
		Map<String, OpInfo> idMap, Collection<InfoTreeGenerator> generators);

	/**
	 * Filters through a list of {@link InfoTreeGenerator}s to find the generator
	 * best suited towards generating {@code signature}
	 * <p>
	 *
	 * @param signature the signature that must be generated
	 * @param generators the list of {@link InfoTreeGenerator}s
	 * @return the {@link InfoTreeGenerator} best suited to the task
	 */
	static InfoTreeGenerator findSuitableGenerator(String signature,
		Collection<InfoTreeGenerator> generators)
	{
		List<InfoTreeGenerator> suitableGenerators = generators.stream() //
			.filter(g -> g.canGenerate(signature)) //
			.sorted() //
			.collect(Collectors.toList());
		if (suitableGenerators.isEmpty()) throw new IllegalArgumentException(
			"No InfoTreeGenerator in given collection " + generators +
				" is able to reify signature " + signature);
		return suitableGenerators.get(0);
	}

	static InfoTree generateDependencyTree(OpEnvironment env, String subsignature,
		Map<String, OpInfo> idMap, Collection<InfoTreeGenerator> generators)
	{
		InfoTreeGenerator genOpt = InfoTreeGenerator.findSuitableGenerator(
			subsignature, generators);
		return genOpt.generate(env, subsignature, idMap, generators);
	}

	/**
	 * Finds the subsignature in {@link String} {@code signature}. The
	 * subsignature is assumed to start at index {@code start}. Note that we
	 * cannot simply use {@link String#substring(int)} because the subsignature
	 * may have dependencies, which are not easily parsed.
	 *
	 * @param signature the signature of some Op
	 * @param start the index where the subsignature starts
	 * @return a signature contained withing {@code signature}
	 */
	static String subSignatureFrom(String signature, int start) {
		int depsStart = signature.indexOf(InfoTree.DEP_START_DELIM, start);
		int depth = 0;
		for (int i = depsStart; i < signature.length(); i++) {
			char ch = signature.charAt(i);
			if (ch == InfoTree.DEP_START_DELIM) depth++;
			else if (ch == InfoTree.DEP_END_DELIM) {
				depth--;
				if (depth == 0) {
					return signature.substring(start, i + 1);
				}
			}
		}
		throw new IllegalArgumentException(
			"There is no complete signature starting from index " + start +
				" in signature " + signature);
	}

	/**
	 * Describes whether this {@link InfoTreeGenerator} is designed to generate
	 * the <b>outer layer</b> of the {@link InfoTree}
	 *
	 * @param signature the signature to use as the template for the
	 *          {@link InfoTree}
	 * @return true iff this {@link InfoTreeGenerator} can generate the outer
	 *         layer of the {@link InfoTree}
	 */
	boolean canGenerate(String signature);

	@Override
	default double priority() {
		return Priority.NORMAL;
	}
}
