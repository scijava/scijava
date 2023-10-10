package org.scijava.ops.engine.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.scijava.ops.api.InfoTree;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.RichOp;

/**
 * An {@link InfoTree} specialization for instances when the dependencies are
 * already instantiated. With a normal {@link InfoTree}, we'd have to get the
 * {@link InfoTree}s of all dependencies, then reinstantiate them.
 *
 * @author Gabe Selzer
 */
public class DependencyRichOpInfoTree extends InfoTree {

	private final List<RichOp<?>> dependencies;

	public DependencyRichOpInfoTree(OpInfo info, List<RichOp<?>> dependencies) {
		super(info, treesFrom(dependencies));
		this.dependencies = dependencies;
	}

	@Override
	protected Object generateOp() {
		return info().createOpInstance(dependencies).object();
	}

	private static List<InfoTree> treesFrom(List<RichOp<?>> dependencies) {
		return dependencies.stream().map(RichOp::infoTree).collect(Collectors.toList());
	}

}
