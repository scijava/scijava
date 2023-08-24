
package org.scijava.ops.engine.matcher.adapt;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.ops.api.InfoChain;
import org.scijava.ops.engine.InfoChainGenerator;
import org.scijava.ops.api.OpInfo;
import org.scijava.types.Types;

public class AdaptationInfoChainGenerator implements InfoChainGenerator {

	@Override
	public InfoChain generate(String signature, Map<String, OpInfo> idMap,
		Collection<InfoChainGenerator> generators)
	{

		// Resolve adaptor
		String adaptorComponent = signature.substring(signature.indexOf(OpAdaptationInfo.ADAPTOR), signature.indexOf(OpAdaptationInfo.ORIGINAL));
		if (!adaptorComponent.startsWith(OpAdaptationInfo.ADAPTOR))
			throw new IllegalArgumentException("Adaptor component " +
				adaptorComponent + " must begin with prefix " +
				OpAdaptationInfo.ADAPTOR);
		String adaptorSignature = adaptorComponent.substring(
			OpAdaptationInfo.ADAPTOR.length());
		InfoChain adaptorChain = InfoChainGenerator.generateDependencyChain(
			adaptorSignature, idMap, generators);

		// Resolve original op
		String originalComponent = signature.substring(signature.indexOf(OpAdaptationInfo.ORIGINAL));
		if (!originalComponent.startsWith(OpAdaptationInfo.ORIGINAL))
			throw new IllegalArgumentException("Original Op component " +
				originalComponent + " must begin with prefix " +
				OpAdaptationInfo.ORIGINAL);
		String originalSignature = originalComponent.substring(
			OpAdaptationInfo.ORIGINAL.length());
		InfoChain originalChain = InfoChainGenerator.generateDependencyChain(
			originalSignature, idMap, generators);

		// Rebuild original chain with an OpAdaptationInfo
		OpInfo originalInfo = originalChain.info();
		// TODO: The op type is wrong!
		Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<>();
		if (!Types.isAssignable(originalInfo.opType(), adaptorChain.info().inputs()
			.get(0).getType(), typeVarAssigns)) throw new IllegalArgumentException(
				"The adaptor cannot be used on Op " + originalInfo);
		Type adaptedOpType = Types.substituteTypeVariables(adaptorChain.info()
			.output().getType(), typeVarAssigns);
		OpInfo adaptedInfo = new OpAdaptationInfo(originalInfo, adaptedOpType,
			adaptorChain);
		return new InfoChain(adaptedInfo, originalChain.dependencies());

	}

	List<String> parseComponents(String signature) {
		List<String> components = new ArrayList<>();
		String s = signature;
		while (s.length() > 0) {
			String subSignatureFrom = InfoChainGenerator.subSignatureFrom(s, 0);
			components.add(subSignatureFrom);
			s = s.substring(subSignatureFrom.length());
		}
		return components;
	}

	@Override
	public boolean canGenerate(String signature) {
		return signature.startsWith(OpAdaptationInfo.IMPL_DECLARATION);
	}

}
