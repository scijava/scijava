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

package org.scijava.ops.engine.matcher.simplify;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.ops.api.*;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.ops.engine.InfoTreeGenerator;
import org.scijava.types.Nil;
import org.scijava.types.Types;

/**
 * Generates a {@link InfoTree}, rooted by a {@link FocusedOpInfo}, from a
 * {@link String} signature.
 *
 * @author Gabriel Selzer
 */
public class FocusedInfoTreeGenerator implements InfoTreeGenerator {

	@Override
	public InfoTree generate(OpEnvironment env, String signature,
		Map<String, OpInfo> idMap, Collection<InfoTreeGenerator> generators)
	{
		// get the list of components
		List<String> components = parseComponents(signature.substring(
			FocusedOpInfo.IMPL_DECLARATION.length()));
		int compIndex = 0;

		// Proceed to input simplifiers
		List<RichOp<Function<?, ?>>> reqSimplifiers = new ArrayList<>();
		String reqSimpComp = components.get(compIndex);
		Hints dependencyHints = new Hints(BaseOpHints.History.IGNORE);
		while (reqSimpComp.startsWith(FocusedOpInfo.INPUT_SIMPLIFIER_DELIMITER)) {
			String reqSimpSignature = reqSimpComp.substring(
				FocusedOpInfo.INPUT_SIMPLIFIER_DELIMITER.length());
			InfoTree reqSimpChain = InfoTreeGenerator.generateDependencyTree(env,
				reqSimpSignature, idMap, generators);
			reqSimplifiers.add(Ops.rich(env.opFromInfoChain(reqSimpChain,
				new Nil<>()
				{}, dependencyHints)));
			reqSimpComp = components.get(++compIndex);
		}

		// Proceed to output focuser
		String outFocuserComp = components.get(compIndex++);
		if (!outFocuserComp.startsWith(FocusedOpInfo.OUTPUT_FOCUSER_DELIMITER))
			throw new IllegalArgumentException("Signature " + signature +
				" does not contain an output simplifier signature (starting with " +
				FocusedOpInfo.OUTPUT_FOCUSER_DELIMITER + ")");
		String outFocuserSignature = outFocuserComp.substring(
			FocusedOpInfo.OUTPUT_FOCUSER_DELIMITER.length());
		InfoTree outputFocuserTree = InfoTreeGenerator.generateDependencyTree(env,
			outFocuserSignature, idMap, generators);
		RichOp<Function<?, ?>> outputFocuser = Ops.rich(env.opFromInfoChain(
			outputFocuserTree, new Nil<>()
			{}, dependencyHints));

		// Proceed to output copier
		RichOp<Computers.Arity1<?, ?>> copier = null;
		String outCopyComp = components.get(compIndex);
		if (!outCopyComp.startsWith(FocusedOpInfo.OUTPUT_COPIER_DELIMITER))
			throw new IllegalArgumentException("Signature " + signature +
				" does not contain an output simplifier signature (starting with " +
				FocusedOpInfo.OUTPUT_COPIER_DELIMITER + ")");
		String outCopySignature = outCopyComp.substring(
			FocusedOpInfo.OUTPUT_COPIER_DELIMITER.length());
		if (!outCopySignature.isEmpty()) {
			InfoTree copierTree = InfoTreeGenerator.generateDependencyTree(env,
				outCopySignature, idMap, generators);
			copier = Ops.rich(env.opFromInfoChain(copierTree, new Nil<>() {},
				dependencyHints));
		}

		// Proceed to original info
		String originalComponent = signature.substring(signature.indexOf(
			FocusedOpInfo.ORIGINAL_INFO));
		if (!originalComponent.startsWith(FocusedOpInfo.ORIGINAL_INFO))
			throw new IllegalArgumentException("Signature " + signature +
				" does not contain an original Op signature (starting with " +
				FocusedOpInfo.ORIGINAL_INFO + ")");
		String originalSignature = originalComponent.substring(
			FocusedOpInfo.ORIGINAL_INFO.length());
		InfoTree originalChain = InfoTreeGenerator.generateDependencyTree(env,
			originalSignature, idMap, generators);
		SimplifiedOpInfo simpleInfo = (SimplifiedOpInfo) originalChain.info();

		// Determine the Op type
		Type[] inTypes = reqSimplifiers.stream().map(op -> Ops.info(op).inputTypes()
			.get(0)).toArray(Type[]::new);
		Type outType = Ops.info(outputFocuser).outputType();
		Type type = SimplificationUtils.retypeOpType(Types.raw(simpleInfo.opType()),
			inTypes, outType);

		OpInfo baseInfo = new FocusedOpInfo((SimplifiedOpInfo) originalChain.info(),
			type, reqSimplifiers, outputFocuser, copier, env);
		return new InfoTree(baseInfo, originalChain.dependencies());
	}

	private List<String> parseComponents(String signature) {
		List<String> components = new ArrayList<>();
		String s = signature;
		while (!s.isEmpty()) {
			String subSignatureFrom = InfoTreeGenerator.subSignatureFrom(s, 0);
			components.add(subSignatureFrom);
			s = s.substring(subSignatureFrom.length());
		}
		return components;
	}

	@Override
	public boolean canGenerate(String signature) {
		return signature.startsWith(FocusedOpInfo.IMPL_DECLARATION);
	}

}
