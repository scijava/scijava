package org.scijava.ops.engine.matcher.simplify;

import org.scijava.ops.api.InfoTree;

public class CompleteMutatorChain extends MutatorChain {

	private final InfoTree simpleChain;
	private final InfoTree focusChain;

	public CompleteMutatorChain(InfoTree simplifier, InfoTree focuser,
		TypePair ioTypes)
	{
		super(simplifier.info(), focuser.info(), ioTypes, null);
		this.simpleChain = simplifier;
		this.focusChain = focuser;
	}

	@Override
	public InfoTree simplifier() {
		return simpleChain;
	}

	@Override
	public InfoTree focuser() {
		return focusChain;
	}

}
