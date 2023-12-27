/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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

package org.scijava.ops.engine.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.scijava.ops.api.InfoTree;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.engine.InfoTreeGenerator;
import org.scijava.ops.api.OpInfo;

public class DefaultInfoTreeGenerator implements InfoTreeGenerator {

	@Override
	public InfoTree generate(OpEnvironment env, String signature, Map<String, OpInfo> idMap,
		Collection<InfoTreeGenerator> generators)
	{
		int dependencyStart = signature.indexOf(InfoTree.DEP_START_DELIM);
		int dependencyEnd = signature.lastIndexOf(InfoTree.DEP_END_DELIM);
		String infoID = signature.substring(0, dependencyStart);
		OpInfo info = idMap.get(infoID);
		if (info == null) throw new IllegalArgumentException(
			"Could not find an OpInfo corresponding to id " + infoID);
		String dependencySignature = signature.substring(dependencyStart + 1,
			dependencyEnd);
		List<String> dependencies = getDependencies(dependencySignature);
		List<InfoTree> dependencyChains = new ArrayList<>();
		for (String dep : dependencies) {
			dependencyChains.add(InfoTreeGenerator.generateDependencyTree(env, dep,
				idMap, generators));
		}
		return new InfoTree(info, dependencyChains);
	}

	private List<String> getDependencies(String signature) {
		int parenDepth = 0;
		int start = 0;
		List<String> splits = new ArrayList<>();

		for (int i = 0; i < signature.length(); i++) {
			char ch = signature.charAt(i);
			if (ch == InfoTree.DEP_START_DELIM) parenDepth++;
			else if (ch == InfoTree.DEP_END_DELIM) {
				parenDepth--;
				if (parenDepth == 0) {
					splits.add(signature.substring(start, i + 1));
					start = i + 1;
				}
			}
		}

		return splits;
	}

	@Override
	public boolean canGenerate(String signature) {
		return signature.startsWith(OpInfo.IMPL_DECLARATION);
	}

}
