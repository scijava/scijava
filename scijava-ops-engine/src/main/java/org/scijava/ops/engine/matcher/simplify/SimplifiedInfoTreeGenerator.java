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

/**
 * Generates a {@link InfoTree}, rooted by a {@link SimplifiedOpInfo}, from a
 * {@link String} signature.
 *
 * @author Gabriel Selzer
 */
public class SimplifiedInfoTreeGenerator implements InfoTreeGenerator {

	@Override
	public InfoTree generate(OpEnvironment env, String signature,
		Map<String, OpInfo> idMap, Collection<InfoTreeGenerator> generators)
	{
		// get the list of components
		List<String> components = parseComponents(signature.substring(
			SimplifiedOpInfo.IMPL_DECLARATION.length()));
		int compIndex = 0;

		List<RichOp<Function<?, ?>>> reqFocusers = new ArrayList<>();
		String reqFocuserComp = components.get(compIndex);
		Hints dependencyHints = new Hints(BaseOpHints.History.IGNORE);
		while (reqFocuserComp.startsWith(
			SimplifiedOpInfo.INPUT_FOCUSER_DELIMITER))
		{
			String reqFocuserSignature = reqFocuserComp.substring(
				SimplifiedOpInfo.INPUT_FOCUSER_DELIMITER.length());
			InfoTree reqFocuserChain = InfoTreeGenerator.generateDependencyTree(env,
				reqFocuserSignature, idMap, generators);

			reqFocusers.add(Ops.rich(env.opFromInfoChain(reqFocuserChain,
				new Nil<>()
				{}, dependencyHints)));
			reqFocuserComp = components.get(++compIndex);
		}
		// Proceed to output simplifier
		String outSimpComp = components.get(compIndex++);
		if (!outSimpComp.startsWith(SimplifiedOpInfo.OUTPUT_SIMPLIFIER_DELIMITER))
			throw new IllegalArgumentException("Signature " + signature +
				" does not contain an output simplifier signature (starting with " +
				SimplifiedOpInfo.OUTPUT_SIMPLIFIER_DELIMITER + ")");
		String outSimpSignature = outSimpComp.substring(
			SimplifiedOpInfo.OUTPUT_SIMPLIFIER_DELIMITER.length());
		InfoTree outputSimplifierChain = InfoTreeGenerator.generateDependencyTree(
			env, outSimpSignature, idMap, generators);
		RichOp<Function<?, ?>> outSimplifier = Ops.rich(env.opFromInfoChain(
			outputSimplifierChain, new Nil<>()
			{}, dependencyHints));

		// Proceed to output copier
		RichOp<Computers.Arity1<?, ?>> copier = null;
		String outCopyComp = components.get(compIndex++);
		if (!outCopyComp.startsWith(SimplifiedOpInfo.OUTPUT_COPIER_DELIMITER))
			throw new IllegalArgumentException("Signature " + signature +
				" does not contain an output simplifier signature (starting with " +
				SimplifiedOpInfo.OUTPUT_COPIER_DELIMITER + ")");
		String outCopySignature = outCopyComp.substring(
			SimplifiedOpInfo.OUTPUT_COPIER_DELIMITER.length());
		if (!outCopySignature.isEmpty()) {
			InfoTree copierTree = InfoTreeGenerator.generateDependencyTree(env,
				outCopySignature, idMap, generators);
			copier = Ops.rich(env.opFromInfoChain(copierTree, new Nil<>() {},
				dependencyHints));
		}

		// Proceed to original info
		String originalComponent = components.get(compIndex);
		if (!originalComponent.startsWith(SimplifiedOpInfo.ORIGINAL_INFO))
			throw new IllegalArgumentException("Signature " + signature +
				" does not contain an original Op signature (starting with " +
				SimplifiedOpInfo.ORIGINAL_INFO + ")");
		String originalSignature = originalComponent.substring(
			SimplifiedOpInfo.ORIGINAL_INFO.length());
		InfoTree originalChain = InfoTreeGenerator.generateDependencyTree(env,
			originalSignature, idMap, generators);

		OpInfo baseInfo = new SimplifiedOpInfo(originalChain.info(), reqFocusers,
			outSimplifier, copier);
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
		return signature.startsWith(SimplifiedOpInfo.IMPL_DECLARATION);
	}

}
