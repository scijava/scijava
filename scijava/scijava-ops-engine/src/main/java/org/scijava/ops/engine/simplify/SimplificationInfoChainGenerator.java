package org.scijava.ops.engine.simplify;

import java.util.Collection;
import java.util.Map;

import org.scijava.ops.api.InfoChain;
import org.scijava.ops.api.InfoChainGenerator;
import org.scijava.ops.api.OpInfo;


public class SimplificationInfoChainGenerator implements InfoChainGenerator {

	@Override
	public InfoChain generate(String signature, Map<String, OpInfo> idMap,
		Collection<InfoChainGenerator> generators)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canGenerate(String signature) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double priority() {
		// TODO Auto-generated method stub
		return 0;
	}

}
