package org.scijava.ops.engine.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.scijava.ops.api.InfoChain;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.RichOp;

/**
 * An {@link InfoChain} specialization for instances when the dependencies are
 * already instantiated. With a normal {@link InfoChain}, we'd have to get the
 * {@link InfoChain}s of all dependencies, then reinstantiate them.
 *
 * @author Gabe Selzer
 */
public class DependencyInstantiatedInfoChain extends InfoChain {

	private final List<RichOp<?>> dependencies;

	public DependencyInstantiatedInfoChain(OpInfo info, List<RichOp<?>> dependencies) {
		super(info, chainsFrom(dependencies));
		this.dependencies = dependencies;
	}

	@Override
	protected Object generateOp() {
		List<Object> dependencyInstances = dependencies.stream() //
			.map(d -> d.op()) //
			.collect(Collectors.toList());
		return info().createOpInstance(dependencyInstances).object();
	}

	public static List<InfoChain> chainsFrom(List<RichOp<?>> dependencies) {
		return dependencies.stream().map(op -> op.infoChain()).collect(Collectors.toList());
	}

}
