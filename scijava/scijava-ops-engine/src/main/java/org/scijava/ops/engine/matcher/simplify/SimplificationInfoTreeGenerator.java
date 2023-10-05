package org.scijava.ops.engine.matcher.simplify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.scijava.ops.api.InfoTree;
import org.scijava.ops.engine.InfoTreeGenerator;
import org.scijava.ops.api.OpInfo;

public class SimplificationInfoTreeGenerator implements InfoTreeGenerator {

	@Override
	public InfoTree generate(String signature, Map<String, OpInfo> idMap,
		Collection<InfoTreeGenerator> generators)
	{
		// get the list of components
		List<String> components = parseComponents(signature.substring(SimplifiedOpInfo.IMPL_DECLARATION.length()));
		int compIndex = 0;

		// Proceed to input simplifiers
		List<InfoTree> refSimplifiers = new ArrayList<>();
		String refSimpComp = components.get(compIndex);
		while (refSimpComp.startsWith(
			SimplifiedOpInfo.INPUT_SIMPLIFIER_DELIMITER))
		{
			String refSimpSignature = refSimpComp.substring(
				SimplifiedOpInfo.INPUT_SIMPLIFIER_DELIMITER.length());
			InfoTree refSimpChain = InfoTreeGenerator.generateDependencyTree(
				refSimpSignature, idMap, generators);
			refSimplifiers.add(refSimpChain);
			refSimpComp = components.get(++compIndex);
		}

		// Proceed to input simplifiers
		List<InfoTree> infoFocusers = new ArrayList<>();
		String infoFocuserComp = components.get(compIndex);
		while (infoFocuserComp.startsWith(
			SimplifiedOpInfo.INPUT_FOCUSER_DELIMITER))
		{
			String infoFocSignature = infoFocuserComp.substring(
				SimplifiedOpInfo.INPUT_FOCUSER_DELIMITER.length());
			InfoTree infoFocChain = InfoTreeGenerator.generateDependencyTree(
				infoFocSignature, idMap, generators);
			infoFocusers.add(infoFocChain);
			infoFocuserComp = components.get(++compIndex);
		}

		if (infoFocusers.size() != refSimplifiers.size())
			throw new IllegalArgumentException("Signature " + signature +
				" does not have the same number of input simplifiers and input focusers!");

		// Proceed to output simplifier
		String outSimpComp = components.get(compIndex++);
		if (!outSimpComp.startsWith(SimplifiedOpInfo.OUTPUT_SIMPLIFIER_DELIMITER))
			throw new IllegalArgumentException("Signature " + signature +
				" does not contain an output simplifier signature (starting with " +
				SimplifiedOpInfo.OUTPUT_SIMPLIFIER_DELIMITER + ")");
		String outSimpSignature = outSimpComp.substring(
			SimplifiedOpInfo.OUTPUT_SIMPLIFIER_DELIMITER.length());
		InfoTree outputSimplifierChain = InfoTreeGenerator
			.generateDependencyTree(outSimpSignature, idMap, generators);

		// Proceed to output focuser
		String outFocComp = components.get(compIndex++);
		if (!outFocComp.startsWith(SimplifiedOpInfo.OUTPUT_FOCUSER_DELIMITER))
			throw new IllegalArgumentException("Signature " + signature +
				" does not contain an output simplifier signature (starting with " +
				SimplifiedOpInfo.OUTPUT_FOCUSER_DELIMITER + ")");
		String outFocSignature = outFocComp.substring(
			SimplifiedOpInfo.OUTPUT_FOCUSER_DELIMITER.length());
		InfoTree outputFocuserChain = InfoTreeGenerator.generateDependencyTree(
			outFocSignature, idMap, generators);

		// Proceed to output copier
		Optional<InfoTree> copierChain;
		String outCopyComp = components.get(compIndex++);
		if (!outCopyComp.startsWith(SimplifiedOpInfo.OUTPUT_COPIER_DELIMITER))
			throw new IllegalArgumentException("Signature " + signature +
				" does not contain an output simplifier signature (starting with " +
				SimplifiedOpInfo.OUTPUT_COPIER_DELIMITER + ")");
		String outCopySignature = outCopyComp.substring(
			SimplifiedOpInfo.OUTPUT_COPIER_DELIMITER.length());
		if (outCopySignature.isEmpty()) copierChain = Optional.empty();
		else {
			copierChain = Optional.of(InfoTreeGenerator.generateDependencyTree(
				outCopySignature, idMap, generators));
		}

		// Proceed to original info
		String originalComponent = components.get(compIndex++);
		if (!originalComponent.startsWith(SimplifiedOpInfo.ORIGINAL_INFO))
			throw new IllegalArgumentException("Signature " + signature +
				" does not contain an original Op signature (starting with " +
				SimplifiedOpInfo.ORIGINAL_INFO + ")");
		String originalSignature = originalComponent.substring(
			SimplifiedOpInfo.ORIGINAL_INFO.length());
		InfoTree originalChain = InfoTreeGenerator.generateDependencyTree(
			originalSignature, idMap, generators);


		SimplificationMetadata metadata = new SimplificationMetadata(originalChain
			.info(), refSimplifiers, infoFocusers, outputSimplifierChain,
			outputFocuserChain, copierChain);
		OpInfo baseInfo = new SimplifiedOpInfo(originalChain.info(), metadata, Double.MIN_VALUE);
		return new InfoTree(baseInfo, originalChain.dependencies());
	}

	List<String> parseComponents(String signature) {
		List<String> components = new ArrayList<>();
		String s = signature;
		while(s.length() > 0) {
			String subSignatureFrom = InfoTreeGenerator.subSignatureFrom(s, 0);
			components.add(subSignatureFrom);
			s = s.substring(subSignatureFrom.length());
		}
		return components;
	}

	@Override
	public boolean canGenerate(String signature) {
		return signature.startsWith(SimplifiedOpInfo.IMPL_DECLARATION);
	}

}
