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

package org.scijava.ops.engine.matcher.convert;

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

import static org.scijava.ops.engine.matcher.convert.ConvertedOpInfo.PRECONVERTER_DELIMITER;

/**
 * Generates a {@link InfoTree}, rooted by a {@link ConvertedOpInfo}, from a
 * {@link String} signature.
 *
 * @author Gabriel Selzer
 */
public class ConvertedInfoTreeGenerator implements InfoTreeGenerator {

	private static final Nil<Function<?, ?>> FUNCTION_NIL //
		= new Nil<>() {};
	private static final Nil<Computers.Arity1<?, ?>> COMPUTER_NIL //
		= new Nil<>() {};

	@Override
	public InfoTree generate(OpEnvironment env, String signature,
		Map<String, OpInfo> idMap, Collection<InfoTreeGenerator> generators)
	{
		// get the list of components
		List<String> components = parseComponents(signature.substring(
			ConvertedOpInfo.IMPL_DECLARATION.length()));
		Hints dependencyHints = new Hints(BaseOpHints.History.IGNORE);

		// For an Op with n inputs, we expect:
		// n preconverters
		// 1 postconverter
		// 1 copy op
		// 1 "original" op
		// Thus we expect (n+3) elements in components

		// preconverters
		List<RichOp<Function<?, ?>>> preconverters = new ArrayList<>();
		int numPreconverters = components.size() - 3;
		for (int i = 0; i < numPreconverters; i++) {
			String reqFocuserComp = components.remove(0);
			String reqFocuserSignature = reqFocuserComp.substring(
				PRECONVERTER_DELIMITER.length());
			InfoTree reqFocuserChain = InfoTreeGenerator.generateDependencyTree(env,
				reqFocuserSignature, idMap, generators);

			preconverters.add(Ops.rich(env.opFromInfoChain(reqFocuserChain,
				FUNCTION_NIL, dependencyHints)));
		}

		// postconverter
		String postconverterComp = components.remove(0);
		if (!postconverterComp.startsWith(ConvertedOpInfo.POSTCONVERTER_DELIMITER))
			throw new IllegalArgumentException("Signature " + signature +
				" does not contain a postconverter signature (starting with " +
				ConvertedOpInfo.POSTCONVERTER_DELIMITER + ")");
		String postconverterSignature = postconverterComp.substring(
			ConvertedOpInfo.POSTCONVERTER_DELIMITER.length());
		InfoTree postconverterChain = InfoTreeGenerator.generateDependencyTree(env,
			postconverterSignature, idMap, generators);
		RichOp<Function<?, ?>> postconverter = Ops.rich(env.opFromInfoChain(
			postconverterChain, FUNCTION_NIL, dependencyHints));

		// output copier
		RichOp<Computers.Arity1<?, ?>> copier = null;
		String outCopyComp = components.remove(0);
		if (!outCopyComp.startsWith(ConvertedOpInfo.OUTPUT_COPIER_DELIMITER))
			throw new IllegalArgumentException("Signature " + signature +
				" does not contain a copier signature (starting with " +
				ConvertedOpInfo.OUTPUT_COPIER_DELIMITER + ")");
		String outCopySignature = outCopyComp.substring(
			ConvertedOpInfo.OUTPUT_COPIER_DELIMITER.length());
		if (!outCopySignature.isEmpty()) {
			InfoTree copierTree = InfoTreeGenerator.generateDependencyTree(env,
				outCopySignature, idMap, generators);
			copier = Ops.rich(env.opFromInfoChain(copierTree, COMPUTER_NIL,
				dependencyHints));
		}

		// Proceed to original info
		String originalComponent = components.remove(0);
		if (!originalComponent.startsWith(ConvertedOpInfo.ORIGINAL_INFO))
			throw new IllegalArgumentException("Signature " + signature +
				" does not contain an original Op signature (starting with " +
				ConvertedOpInfo.ORIGINAL_INFO + ")");
		String originalSignature = originalComponent.substring(
			ConvertedOpInfo.ORIGINAL_INFO.length());
		InfoTree originalChain = InfoTreeGenerator.generateDependencyTree(env,
			originalSignature, idMap, generators);

		OpInfo baseInfo = new ConvertedOpInfo(originalChain.info(), preconverters,
			postconverter, copier, env);
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
		return signature.startsWith(ConvertedOpInfo.IMPL_DECLARATION);
	}

}
