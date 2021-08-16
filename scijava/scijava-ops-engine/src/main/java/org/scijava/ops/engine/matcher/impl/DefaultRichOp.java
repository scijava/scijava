
package org.scijava.ops.engine.matcher.impl;

import org.scijava.ops.api.OpExecutionSummary;
import org.scijava.ops.api.OpMetadata;
import org.scijava.ops.api.RichOp;
import org.scijava.ops.api.features.BaseOpHints.DependencyMatching;

public abstract class DefaultRichOp implements RichOp {

	private final Object op;
	private final OpMetadata metadata;

	public DefaultRichOp(final Object op, final OpMetadata metadata) {
		this.op = op;
		this.metadata = metadata;
	}

	@Override
	public Object op() {
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
			OpExecutionSummary e = new OpExecutionSummary(this, output);
			metadata.history().addExecution(e);
		}
	}

}
