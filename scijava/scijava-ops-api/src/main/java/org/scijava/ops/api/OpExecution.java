package org.scijava.ops.api;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class OpExecution {

	private final RichOp<?> op;
	private ProgressReporter reporter;
	private final CompletableFuture<Object> outContainer;

	public OpExecution(RichOp<?> op) {
		this.op = op;
		this.outContainer = new CompletableFuture<>();
	}

	public void setReporter(ProgressReporter p) {
		this.reporter = p;
	}

	public RichOp<?> op() {
		return op;
	}

	public ProgressReporter reporter() {
		return reporter;
	}

	public void recordCompletion(Object output) {
		outContainer.complete(output);
		reporter.reportCompletion();
	}

	public Future<Object> output() {
		return outContainer;
	}


}
