
package org.scijava.ops.engine.matcher.adapt;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.scijava.ops.api.InfoTree;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.InfoTreeGenerator;
import org.scijava.types.Types;

public class AdaptationInfoTreeGenerator implements InfoTreeGenerator {

	@Override
	public InfoTree generate(String signature, Map<String, OpInfo> idMap,
		Collection<InfoTreeGenerator> generators)
	{

		// Resolve adaptor
		String adaptorComponent = signature.substring(signature.indexOf(OpAdaptationInfo.ADAPTOR), signature.indexOf(OpAdaptationInfo.ORIGINAL));
		if (!adaptorComponent.startsWith(OpAdaptationInfo.ADAPTOR))
			throw new IllegalArgumentException("Adaptor component " +
				adaptorComponent + " must begin with prefix " +
				OpAdaptationInfo.ADAPTOR);
		String adaptorSignature = adaptorComponent.substring(
			OpAdaptationInfo.ADAPTOR.length());
		InfoTree adaptorTree = InfoTreeGenerator.generateDependencyTree(
			adaptorSignature, idMap, generators);

		// Resolve original op
		String originalComponent = signature.substring(signature.indexOf(OpAdaptationInfo.ORIGINAL));
		if (!originalComponent.startsWith(OpAdaptationInfo.ORIGINAL))
			throw new IllegalArgumentException("Original Op component " +
				originalComponent + " must begin with prefix " +
				OpAdaptationInfo.ORIGINAL);
		String originalSignature = originalComponent.substring(
			OpAdaptationInfo.ORIGINAL.length());
		InfoTree originalTree = InfoTreeGenerator.generateDependencyTree(
			originalSignature, idMap, generators);

		// Rebuild original tree with an OpAdaptationInfo
		OpInfo originalInfo = originalTree.info();
		// TODO: The op type is wrong!
		Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<>();
		if (!Types.isAssignable(originalInfo.opType(), adaptorTree.info().inputs()
			.get(0).getType(), typeVarAssigns)) throw new IllegalArgumentException(
				"The adaptor cannot be used on Op " + originalInfo);
		Type adaptedOpType = Types.substituteTypeVariables(adaptorTree.info()
			.output().getType(), typeVarAssigns);
		OpInfo adaptedInfo = new OpAdaptationInfo(originalInfo, adaptedOpType,
			adaptorTree);
		return new InfoTree(adaptedInfo, originalTree.dependencies());

	}

	@Override
	public boolean canGenerate(String signature) {
		return signature.startsWith(OpAdaptationInfo.IMPL_DECLARATION);
	}

}
