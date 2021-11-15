package org.scijava.ops.engine.simplify;

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
import org.scijava.ops.api.OpRef;
import org.scijava.ops.api.OpUtils;
import org.scijava.types.Types;


public class InfoSimplificationGenerator {

	private final OpInfo info;
	private final OpEnvironment env;
	private final List<List<OpInfo>> focuserSets;
	private final List<OpInfo> outputSimplifiers;

	public InfoSimplificationGenerator(OpInfo info, OpEnvironment env) {
		this.info = info;
		this.env = env;
		Type[] args = OpUtils.inputs(info.struct()).stream() //
				.map(m -> m.getType()) //
				.toArray(Type[]::new);
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

	public OpInfo generateSuitableInfo(OpEnvironment env, OpRef originalRef, Hints hints) {
		SimplifiedOpRef simpleRef = SimplifiedOpRef.simplificationOf(env, originalRef, hints);
		return generateSuitableInfo(simpleRef);
	}

	public OpInfo generateSuitableInfo(SimplifiedOpRef ref) {
		if(!Types.isAssignable(Types.raw(info.opType()), ref.rawType()))
				throw new IllegalArgumentException("OpInfo and OpRef do not share an Op type");
		TypePair[] argPairings = generatePairings(ref);
		TypePair outPairing = generateOutPairing(ref);
		Map<TypePair, ChainCluster> pathways = findPathways(ref, argPairings, outPairing);
		validateClusters(pathways);

		Map<TypePair, MutatorChain> chains = new HashMap<>();
		pathways.forEach((pair, cluster) -> chains.put(pair, resolveCluster(cluster)));

		SimplificationMetadata metadata = new SimplificationMetadata(ref, info, argPairings, outPairing, chains);
		return new SimplifiedOpInfo(info, env, metadata);
	}

	private TypePair[] generatePairings(SimplifiedOpRef ref)
	{
		Type[] originalArgs = ref.srcRef().getArgs();
		Type[] originalParams = info.inputs().stream().map(m -> m.getType())
			.toArray(Type[]::new);
		TypePair[] pairings = Streams.zip(Arrays.stream(originalArgs), Arrays
			.stream(originalParams), (from, to) -> new TypePair(from, to)).toArray(
				TypePair[]::new);
		return pairings;
	}

	private TypePair generateOutPairing(SimplifiedOpRef ref) {
		return new TypePair(info.output().getType(), ref.srcRef().getOutType());
	}

	private Map<TypePair, ChainCluster> findPathways(SimplifiedOpRef ref, TypePair[] argPairings, TypePair outPairing) {
		if (ref.srcRef().getArgs().length != info.inputs().size())
			throw new IllegalArgumentException(
				"ref and info must have the same number of arguments!");
		int numInputs = ref.srcRef().getArgs().length;

		Map<TypePair, ChainCluster> pathways = new HashMap<>();

		List<List<OpInfo>> infoFocusers = focuserSets;
		for (int i = 0; i < numInputs; i++) {
			TypePair pairing = argPairings[i];
			if (pathways.keySet().contains(pairing)) continue;
			List<OpInfo> simplifiers = ref.simplifierSets().get(i);
			List<OpInfo> focusers = infoFocusers.get(i);
			ChainCluster cluster = ChainCluster.generateCluster(pairing, simplifiers, focusers, env);
			pathways.put(pairing, cluster);
		}

		if(!pathways.keySet().contains(outPairing)) {
			List<OpInfo> simplifiers = outputSimplifiers;
			List<OpInfo> focusers = ref.outputFocusers();
			ChainCluster cluster = ChainCluster.generateCluster(outPairing, simplifiers, focusers, env);
			pathways.put(outPairing, cluster);
		}

		return pathways;
	}

	/**
	 * If any clusters have no chains from the input type to the output type, then
	 * there does not exist a simplification pathway to mutate the ref type into
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
