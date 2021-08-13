
package org.scijava.ops.engine.matcher.impl;

import java.lang.reflect.Type;
import java.util.UUID;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpExecutionSummary;
import org.scijava.ops.api.OpHistory;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpMetadata;
import org.scijava.ops.api.RichOp;
import org.scijava.ops.api.features.BaseOpHints.DependencyMatching;

public class DefaultRichOp implements RichOp {

	private final Object op;
	private final OpMetadata metadata;

	public DefaultRichOp(final Object op, final OpMetadata metadata)
	{
		this.op = op;
		this.metadata = metadata;
	}

	@Override
	public OpHistory history() {
		return metadata.history();
	}

	@Override
	public OpInfo info() {
		return metadata.info();
	}

	@Override
	public UUID executionID() {
		return metadata.executionID();
	}

	@Override
	public Hints hints() {
		return metadata.hints();
	}

	@Override
	public void preprocess(Object... inputs) {}

	@Override
	public void postprocess(Object output) {
		// Log a new execution
		if (!hints().containsHint(DependencyMatching.IN_PROGRESS)) {
			OpExecutionSummary e = new OpExecutionSummary(executionID(), info(), op, this,
				output);
			history().addExecution(e);
		}
	}

	@Override
	public Type getType() {
		return metadata.type();
	}

}
