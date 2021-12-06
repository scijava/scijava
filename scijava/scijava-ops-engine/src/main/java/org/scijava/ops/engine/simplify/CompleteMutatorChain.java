package org.scijava.ops.engine.simplify;

import org.scijava.ops.api.InfoChain;

public class CompleteMutatorChain extends MutatorChain {

	private final InfoChain simpleChain;
	private final InfoChain focusChain;

	public CompleteMutatorChain(InfoChain simplifier, InfoChain focuser,
		TypePair ioTypes)
	{
		super(simplifier.info(), focuser.info(), ioTypes, null);
		this.simpleChain = simplifier;
		this.focusChain = focuser;
	}

	@Override
	public InfoChain simplifier() {
		return simpleChain;
	}

	@Override
	public InfoChain focuser() {
		return focusChain;
	}

}
