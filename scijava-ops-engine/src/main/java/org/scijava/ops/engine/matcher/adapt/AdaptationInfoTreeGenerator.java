/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2025 SciJava developers.
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

package org.scijava.ops.engine.matcher.adapt;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.scijava.ops.api.InfoTree;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.InfoTreeGenerator;
import org.scijava.common3.Types;

public class AdaptationInfoTreeGenerator implements InfoTreeGenerator {

	@Override
	public InfoTree generate(OpEnvironment env, String signature,
		Map<String, OpInfo> idMap, Collection<InfoTreeGenerator> generators)
	{

		// Resolve adaptor
        var adaptorComponent = signature.substring(signature.indexOf(
			OpAdaptationInfo.ADAPTOR), signature.indexOf(OpAdaptationInfo.ORIGINAL));
		if (!adaptorComponent.startsWith(OpAdaptationInfo.ADAPTOR))
			throw new IllegalArgumentException("Adaptor component " +
				adaptorComponent + " must begin with prefix " +
				OpAdaptationInfo.ADAPTOR);
        var adaptorSignature = adaptorComponent.substring(
			OpAdaptationInfo.ADAPTOR.length());
        var adaptorTree = InfoTreeGenerator.generateDependencyTree(env,
			adaptorSignature, idMap, generators);

		// Resolve original op
        var originalComponent = signature.substring(signature.indexOf(
			OpAdaptationInfo.ORIGINAL));
		if (!originalComponent.startsWith(OpAdaptationInfo.ORIGINAL))
			throw new IllegalArgumentException("Original Op component " +
				originalComponent + " must begin with prefix " +
				OpAdaptationInfo.ORIGINAL);
        var originalSignature = originalComponent.substring(
			OpAdaptationInfo.ORIGINAL.length());
        var originalTree = InfoTreeGenerator.generateDependencyTree(env,
			originalSignature, idMap, generators);

		// Rebuild original tree with an OpAdaptationInfo
        var originalInfo = originalTree.info();
		// TODO: The op type is wrong!
		Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<>();
		if (!Types.isAssignable(originalInfo.opType(), adaptorTree.info().inputs()
			.get(0).type(), typeVarAssigns)) throw new IllegalArgumentException(
				"The adaptor cannot be used on Op " + originalInfo);
        var adaptedOpType = Types.unroll(adaptorTree.info()
			.output().type(), typeVarAssigns);
		OpInfo adaptedInfo = new OpAdaptationInfo(originalInfo, adaptedOpType,
			adaptorTree);
		return new InfoTree(adaptedInfo, originalTree.dependencies());

	}

	@Override
	public boolean canGenerate(String signature) {
		return signature.startsWith(OpAdaptationInfo.IMPL_DECLARATION);
	}

}
