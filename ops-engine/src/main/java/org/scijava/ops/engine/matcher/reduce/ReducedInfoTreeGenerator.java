/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2025 SciJava developers.
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

import org.scijava.ops.api.InfoTree;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.InfoTreeGenerator;

import java.util.Collection;
import java.util.Map;

/**
 * An {@link org.scijava.ops.engine.InfoTreeGenerator} that is used to generate
 * {@link org.scijava.ops.api.InfoTree}s for {@link ReducedOpInfo}s.
 *
 * @author Gabriel Selzer
 */
public class ReducedInfoTreeGenerator implements InfoTreeGenerator {

	@Override
	public InfoTree generate(OpEnvironment env, String signature,
		Map<String, OpInfo> idMap, Collection<InfoTreeGenerator> generators)
	{
		// Chop off the prefix on all ReducedOpInfos
		var reductionPrefix = ReducedOpInfo.IMPL_DECLARATION //
			+ ReducedOpInfo.PARAMS_REDUCED;
		var prefixless = signature.substring(reductionPrefix.length());
		// Find the number of parameters reduced
		var numString = prefixless.substring(0, //
			prefixless.indexOf(ReducedOpInfo.ORIGINAL_INFO));
		var idx = Integer.parseInt(numString) - 1;

		// Create an InfoTree from the original Info
		var substring = signature.substring( //
			signature.indexOf(ReducedOpInfo.ORIGINAL_INFO) //
				+ ReducedOpInfo.ORIGINAL_INFO.length());
		var original = InfoTreeGenerator.generateDependencyTree(env, substring,
			idMap, generators);
		// Reduce the original info, and build a new InfoTree from the proper
		// ReducedInfo
		var reductions = new ReducedOpInfoGenerator().generateInfosFrom(
			original.info());
		return new InfoTree(reductions.get(idx), original.dependencies());
	}

	@Override
	public boolean canGenerate(String signature) {
		return signature.startsWith(ReducedOpInfo.IMPL_DECLARATION);
	}
}
