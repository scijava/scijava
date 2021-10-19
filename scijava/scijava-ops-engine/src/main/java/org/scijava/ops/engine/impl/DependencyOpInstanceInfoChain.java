package org.scijava.ops.engine.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.scijava.ops.api.InfoChain;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpInstance;

/**
 * An {@link InfoChain} specialization for instances when the dependencies are
 * already instantiated. With a normal {@link InfoChain}, we'd have to get the
 * {@link InfoChain}s of all dependencies, then reinstantiate them.
 *
 * @author Gabe Selzer
 */
public class DependencyOpInstanceInfoChain extends InfoChain {

	private final List<OpInstance<?>> dependencies;

	public DependencyOpInstanceInfoChain(OpInfo info, List<OpInstance<?>> dependencies) {
		super(info, chainsFrom(dependencies));
		this.dependencies = dependencies;
	}

	@Override
	protected Object generateOp() {
		List<Object> depObjects = dependencies.stream().map(instance -> instance
			.op()).collect(Collectors.toList());
		return info().createOpInstance(depObjects).object();
	}

	public static List<InfoChain> chainsFrom(List<OpInstance<?>> dependencies) {
		return dependencies.stream().map(op -> op.infoChain()).collect(Collectors.toList());
	}

}
