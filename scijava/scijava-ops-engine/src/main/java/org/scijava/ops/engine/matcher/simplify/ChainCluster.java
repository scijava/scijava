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

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;

public class ChainCluster {

	private final List<MutatorChain> chains;
	private final TypePair pairing;

	private ChainCluster(TypePair pairing) {
		this.chains = new ArrayList<>();
		this.pairing = pairing;
	}

	public static ChainCluster generateCluster(TypePair pairing,
		List<OpInfo> simplifiers, List<OpInfo> focusers, OpEnvironment env)
	{
		ChainCluster cluster = new ChainCluster(pairing);
		List<List<OpInfo>> chains = Lists.cartesianProduct(simplifiers, focusers);

		for (List<OpInfo> chainList : chains) {
			OpInfo simplifier = chainList.get(0);
			OpInfo focuser = chainList.get(1);
			MutatorChain chain = new MutatorChain(simplifier, focuser, pairing, env);
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
