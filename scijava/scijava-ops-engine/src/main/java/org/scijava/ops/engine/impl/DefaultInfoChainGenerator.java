
package org.scijava.ops.engine.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.scijava.ops.api.InfoChain;
import org.scijava.ops.api.InfoChainGenerator;
import org.scijava.ops.api.OpInfo;
import org.scijava.plugin.Plugin;

@Plugin(type = InfoChainGenerator.class)
public class DefaultInfoChainGenerator implements InfoChainGenerator {

	@Override
	public InfoChain generate(String signature, Map<String, OpInfo> idMap,
		Collection<InfoChainGenerator> generators)
	{
		int dependencyStart = signature.indexOf(DEP_START_DELIM);
		int dependencyEnd = signature.lastIndexOf(DEP_END_DELIM);
		String infoID = signature.substring(0, dependencyStart);
		OpInfo info = idMap.get(infoID);
		if (info == null) throw new IllegalArgumentException(
			"Could not find an OpInfo corresponding to id " + infoID);
		String dependencySignature = signature.substring(dependencyStart + 1,
			dependencyEnd);
		List<String> dependencies = getDependencies(dependencySignature);
		List<InfoChain> dependencyChains = new ArrayList<>();
		for (String dep : dependencies) {
			dependencyChains.add(InfoChainGenerator.generateDependencyChain(dep,
				idMap, generators));
		}
		return new InfoChain(info, dependencyChains);
	}

	private List<String> getDependencies(String signature) {
		int parenDepth = 0;
		int start = 0;
		List<String> splits = new ArrayList<>();

		for (int i = 0; i < signature.length(); i++) {
			char ch = signature.charAt(i);
			if (ch == DEP_START_DELIM) parenDepth++;
			else if (ch == DEP_END_DELIM) {
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
		return true;
	}

	@Override
	public double priority() {
		return -(1e10);
	}

}
