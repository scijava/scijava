
package org.scijava.ops.engine.matcher.impl;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.scijava.ops.api.InfoChain;
import org.scijava.ops.api.InfoChainGenerator;
import org.scijava.ops.api.OpInfo;
import org.scijava.plugin.Plugin;
import org.scijava.types.Types;

@Plugin(type = InfoChainGenerator.class)
public class AdaptationInfoChainGenerator implements InfoChainGenerator {

	@Override
	public InfoChain generate(String signature, Map<String, OpInfo> idMap,
		Collection<InfoChainGenerator> generators)
	{
		// Resolve adaptor
		int start = 0;
		int end = matchingCurlyBrace(signature, signature.indexOf('{'));
		String adaptorSignature = signature.substring(start, end + 1);
		Optional<InfoChainGenerator> adaptorGenOpt = InfoChainGenerator.findSuitableGenerator(adaptorSignature, generators);
		if (adaptorGenOpt == null) throw new IllegalArgumentException("Could not find an InfoChainGenerator able to handle id" + adaptorSignature);
		InfoChain adaptorChain = adaptorGenOpt.get().generate(adaptorSignature, idMap, generators);
		
		// Resolve adapted Op
		int delim_start = signature.indexOf(OpAdaptationInfo.IMPL_DELIMITER);
		if (delim_start == -1) throw new IllegalStateException("Op signature " +
			signature + " must have signature delimiter " +
			OpAdaptationInfo.IMPL_DELIMITER +
			", but does not! Was InfoChainGenerator.canGenerate() called?");
		int originalOpStart = delim_start + OpAdaptationInfo.IMPL_DELIMITER.length();
		String originalSignature = signature.substring(originalOpStart);
		Optional<InfoChainGenerator> originalGenOpt = InfoChainGenerator.findSuitableGenerator(originalSignature, generators);
		if (originalGenOpt == null) throw new IllegalArgumentException("Could not find an InfoChainGenerator able to handle id" + originalSignature);
		InfoChain originalChain = originalGenOpt.get().generate(originalSignature, idMap, generators);

		// Rebuild original chain with an OpAdaptationInfo
		OpInfo originalInfo = originalChain.info();
		// TODO: The op type is wrong!
		Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<>();
		if (!Types.isAssignable(originalInfo.opType(), adaptorChain.info().inputs().get(0).getType(), typeVarAssigns)) throw new IllegalArgumentException("The adaptor cannot be used on Op " + originalInfo);
		Type adaptedOpType = Types.substituteTypeVariables(adaptorChain.info().output().getType(), typeVarAssigns);
		OpInfo adaptedInfo = new OpAdaptationInfo(originalInfo, adaptedOpType, adaptorChain);
		return new InfoChain(adaptedInfo, originalChain.dependencies());

	}

	/**
	 * To prevent intensive signature validation, we do two passes:
	 * <ol>
	 * <li>If there is no adaptation delimiter, return false fast
	 * <li>If there is an adaptation delimiter, return true iff it is the
	 * outermost piece
	 * </ol>
	 */
	@Override
	public boolean canGenerate(String signature) {
		// Pass 1 - fail fast iff no adapted Ops
		if (!signature.contains(OpAdaptationInfo.IMPL_DELIMITER)) return false;

		// Pass 2 - return true iff adaptor delimiter directly follows adaptor
		// signature.
		int adaptorDepsStart = signature.indexOf('{');
		int adaptorDepsEnd = matchingCurlyBrace(signature, adaptorDepsStart);
		return signature.indexOf(
			OpAdaptationInfo.IMPL_DELIMITER) == adaptorDepsEnd + 1;
	}

	private int matchingCurlyBrace(String s, int startBraceIndex) {
		int braceCount = 0;
		for (int i = startBraceIndex; i < s.length(); i++) {
			if (s.charAt(i) == DEP_START_DELIM) braceCount++;
			else if (s.charAt(i) == DEP_END_DELIM) {
				braceCount--;
				if (braceCount == 0) return i;
			}
		}
		throw new IllegalArgumentException("Signature" + s +
			" does not have a curly brace matching the one at index " +
			startBraceIndex);
	}

	@Override
	public double priority() {
		return 0;
	}

}
