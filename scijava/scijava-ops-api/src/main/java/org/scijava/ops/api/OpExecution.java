package org.scijava.ops.api;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class OpExecution {

	private final RichOp<?> op;
	private final CompletableFuture<Object> outContainer;

	public OpExecution(RichOp<?> op) {
		this.op = op;
		this.outContainer = new CompletableFuture<>();
	}

	public RichOp<?> op() {
		return op;
	}

	public void recordCompletion(Object output) {
		outContainer.complete(output);
	}

	public Future<Object> output() {
		return outContainer;
	}


}
