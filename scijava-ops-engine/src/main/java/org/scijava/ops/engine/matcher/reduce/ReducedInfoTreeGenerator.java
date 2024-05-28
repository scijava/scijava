
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
