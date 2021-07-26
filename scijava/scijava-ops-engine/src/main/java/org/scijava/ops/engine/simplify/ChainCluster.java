
package org.scijava.ops.engine.simplify;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import org.scijava.ops.api.OpInfo;

public class ChainCluster {

	private final List<MutatorChain> chains;
	private final TypePair pairing;

	private ChainCluster(TypePair pairing) {
		this.chains = new ArrayList<>();
		this.pairing = pairing;
	}

	public static ChainCluster generateCluster(TypePair pairing,
		List<OpInfo> simplifiers, List<OpInfo> focusers)
	{
		ChainCluster cluster = new ChainCluster(pairing);
		List<List<OpInfo>> chains = Lists.cartesianProduct(simplifiers, focusers);

		for (List<OpInfo> chainList : chains) {
			OpInfo simplifier = chainList.get(0);
			OpInfo focuser = chainList.get(1);
			MutatorChain chain = new MutatorChain(simplifier, focuser, pairing);
			if (chain.isValid()) cluster.addChain(chain);
		}
		return cluster;
	}

	public boolean addChain(MutatorChain chain) {
		return chains.add(chain);
	}

	public List<MutatorChain> getChains() {
		return chains;
	}

	public TypePair getPairing() {
		return pairing;
	}
}
