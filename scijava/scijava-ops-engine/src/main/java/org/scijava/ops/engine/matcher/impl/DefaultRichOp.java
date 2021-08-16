
package org.scijava.ops.engine.matcher.impl;

import org.scijava.ops.api.OpMetadata;
import org.scijava.ops.api.RichOp;
import org.scijava.ops.api.features.BaseOpHints.DependencyMatching;

public abstract class DefaultRichOp<T> implements RichOp<T> {

	private final T op;
	private final OpMetadata metadata;

	public DefaultRichOp(final T op, final OpMetadata metadata) {
		this.op = op;
		this.metadata = metadata;
	}

	@Override
	public T op() {
		return op;
	}

	@Override
	public OpMetadata metadata() {
		return metadata;
	}

	@Override
	public void preprocess(Object... inputs) {}

	@Override
	public void postprocess(Object output) {
		// Log a new execution
		if (!metadata.hints().containsHint(DependencyMatching.IN_PROGRESS)) {
			metadata.history().addExecution(this, output);
		}
	}

}
