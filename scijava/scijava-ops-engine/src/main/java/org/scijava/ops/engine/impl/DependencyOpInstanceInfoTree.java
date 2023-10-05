package org.scijava.ops.engine.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.scijava.ops.api.InfoTree;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpInstance;

/**
 * An {@link InfoTree} specialization for instances when the dependencies are
 * already instantiated. With a normal {@link InfoTree}, we'd have to get the
 * {@link InfoTree}s of all dependencies, then reinstantiate them.
 *
 * @author Gabe Selzer
 */
public class DependencyOpInstanceInfoTree extends InfoTree {

	private final List<OpInstance<?>> dependencies;

	public DependencyOpInstanceInfoTree(OpInfo info, List<OpInstance<?>> dependencies) {
		super(info, treesFrom(dependencies));
		this.dependencies = dependencies;
	}

	@Override
	protected Object generateOp() {
		List<Object> depObjects = dependencies.stream() //
			.map(OpInstance::op) //
			.collect(Collectors.toList());
		return info().createOpInstance(depObjects).object();
	}

	private static List<InfoTree> treesFrom(List<OpInstance<?>> dependencies) {
		return dependencies.stream().map(OpInstance::infoTree).collect(Collectors.toList());
	}

}
