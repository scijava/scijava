package org.scijava.ops.provenance;

import org.scijava.ops.OpInfo;

public class OpExecutionSummary implements ExecutionSummary<Object> {

	private final OpInfo info;
	private final Object instance;
	private final Object output;

	public OpExecutionSummary(OpInfo info, Object op, Object output) {
		this.info = info;
		this.instance = op;
		this.output = output;
	}

	@Override
	public Object output() {
		return output;
	}

	@Override
	public Object executor() {
		return instance;
	}

	@Override
	public boolean isOutput(Object o) {
		return output == o;
	}

	public OpInfo info() {
		return info;
	}

}
