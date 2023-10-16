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

import com.google.common.collect.Streams;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpRequest;
import org.scijava.struct.Member;
import org.scijava.types.Types;


public class InfoSimplificationGenerator {

	private final OpInfo info;
	private final OpEnvironment env;
	private final List<List<OpInfo>> focuserSets;
	private final List<OpInfo> outputSimplifiers;

	public InfoSimplificationGenerator(OpInfo info, OpEnvironment env) {
		this.info = info;
		this.env = env;
		Type[] args = info.inputTypes().toArray(Type[]::new);
		this.focuserSets = SimplificationUtils.focusArgs(env, args);
		Type outType = info.output().getType();
		this.outputSimplifiers = SimplificationUtils.getSimplifiers(env, outType);
	}

	public List<List<OpInfo>> getFocusers(){
		return focuserSets;
	}

	public List<OpInfo> getOutputSimplifiers(){
		return outputSimplifiers;
	}

	public OpInfo srcInfo() {
		return info;
	}

	public SimplifiedOpInfo generateSuitableInfo(OpEnvironment env, OpRequest originalReq, Hints hints) {
		SimplifiedOpRequest
				simpleReq = SimplifiedOpRequest.simplificationOf(env, originalReq, hints);
		return generateSuitableInfo(simpleReq);
	}

	public SimplifiedOpInfo generateSuitableInfo(SimplifiedOpRequest req) {
		if(!Types.isAssignable(Types.raw(info.opType()), req.rawType()))
				throw new IllegalArgumentException("OpInfo and OpRequest do not share an Op type");
		TypePair[] argPairings = generatePairings(req);
		TypePair outPairing = generateOutPairing(req);
		Map<TypePair, ChainCluster> pathways = findPathways(req, argPairings, outPairing);
		validateClusters(pathways);

		Map<TypePair, MutatorChain> chains = new HashMap<>();
		pathways.forEach((pair, cluster) -> chains.put(pair, resolveCluster(cluster)));

		SimplificationMetadata metadata = new SimplificationMetadata(req, info, argPairings, outPairing, chains);
		return new SimplifiedOpInfo(info, env, metadata);
	}

	private TypePair[] generatePairings(SimplifiedOpRequest req)
	{
		Type[] originalArgs = req.srcReq().getArgs();
		Type[] originalParams = info.inputs().stream().map(Member::getType)
			.toArray(Type[]::new);
		TypePair[] pairings = Streams.zip(Arrays.stream(originalArgs), Arrays
			.stream(originalParams), TypePair::new).toArray(
				TypePair[]::new);
		return pairings;
	}

	private TypePair generateOutPairing(SimplifiedOpRequest req) {
		return new TypePair(info.output().getType(), req.srcReq().getOutType());
	}

	private Map<TypePair, ChainCluster> findPathways(SimplifiedOpRequest req, TypePair[] argPairings, TypePair outPairing) {
		if (req.srcReq().getArgs().length != info.inputs().size())
			throw new IllegalArgumentException(
				"req and info must have the same number of arguments!");
		int numInputs = req.srcReq().getArgs().length;

		Map<TypePair, ChainCluster> pathways = new HashMap<>();

		for (int i = 0; i < numInputs; i++) {
			TypePair pairing = argPairings[i];
			if (pathways.containsKey(pairing)) continue;
			List<OpInfo> simplifiers = req.simplifierSets().get(i);
			List<OpInfo> focusers = focuserSets.get(i);
			ChainCluster cluster = ChainCluster.generateCluster(pairing, simplifiers, focusers, env);
			pathways.put(pairing, cluster);
		}

		if(!pathways.containsKey(outPairing)) {
			List<OpInfo> focusers = req.outputFocusers();
			ChainCluster cluster = ChainCluster.generateCluster(outPairing,
					outputSimplifiers, focusers, env);
			pathways.put(outPairing, cluster);
		}

		return pathways;
	}

	/**
	 * If any clusters have no chains from the input type to the output type, then
	 * there does not exist a simplification pathway to mutate the request type into
	 * the info type. Thus the arg types do not "match"
	 */
	private void validateClusters(Map<TypePair, ChainCluster> pathways) {
		Optional<ChainCluster> invalidPath = pathways.values().parallelStream() //
			.filter(cluster -> cluster.getChains().size() == 0).findAny();
		if(invalidPath.isPresent()) {
			TypePair unresolvablePair = invalidPath.get().getPairing();
			throw new IllegalArgumentException(
				"Cannot generate simplification: No mutation pathway exists from Type " +
					unresolvablePair.getA() + " to Type " + unresolvablePair.getB());
		}
	}

	private static MutatorChain resolveCluster(ChainCluster cluster) {
		List<MutatorChain> chains = cluster.getChains();
		switch (chains.size()) {
			case 0:
				throw new IllegalArgumentException(
					"ChainCluster should have 1+ chains but has 0 chains!");
			case 1:
				return chains.get(0);
			default:
				// NB max returns Optional iff chains is empty, which it is not.
				return chains.stream().max(MutatorChain::compareTo).get();
		}
	}

}
