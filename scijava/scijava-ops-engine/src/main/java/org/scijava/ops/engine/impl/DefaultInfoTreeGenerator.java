
package org.scijava.ops.engine.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.scijava.ops.api.InfoTree;
import org.scijava.ops.engine.InfoTreeGenerator;
import org.scijava.ops.api.OpInfo;

public class DefaultInfoTreeGenerator implements InfoTreeGenerator {

	@Override
	public InfoTree generate(String signature, Map<String, OpInfo> idMap,
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
			dependencyChains.add(InfoTreeGenerator.generateDependencyTree(dep,
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
