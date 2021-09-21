
package org.scijava.ops.engine.matcher.impl;

import java.lang.reflect.Type;

import org.scijava.ops.api.OpExecution;
import org.scijava.ops.api.OpInstance;
import org.scijava.ops.api.OpMetadata;
import org.scijava.ops.api.RichOp;
import org.scijava.ops.api.features.BaseOpHints.History;
import org.scijava.ops.engine.progress.BinaryProgressReporter;
import org.scijava.ops.engine.progress.Progress;
import org.scijava.ops.engine.progress.ProgressReporters;

/**
 * An abstract implementation of {@link RichOp}. While this class has <b>no
 * abstract methods</b>, it should remain {@code abstract} due to the fact that
 * it does not implement the Op type it purports to be. The implementation of
 * that method is left to implementations of this class (and is necessary for
 * the correct behavior of {@link #asOpType()}).
 * 
 * @author Gabriel Selzer
 * @param <T> the functional {@link Type} of the Op
 */
public abstract class AbstractRichOp<T> implements RichOp<T> {

	private final OpInstance<T> instance;
	private final OpMetadata metadata;

	public AbstractRichOp(final OpInstance<T> instance, final OpMetadata metadata) {
		this.instance = instance;
		this.metadata = metadata;

		if (!metadata.hints().contains(History.SKIP_RECORDING)) {
			metadata.history().logOp(this);
		}
	}

	@Override
	public OpInstance<T> instance() {
		return instance;
	}

	@Override
	public OpMetadata metadata() {
		return metadata;
	}

	@Override
	public void preprocess(Object... inputs) {
		OpExecution e = new OpExecution(this);
		e.setReporter(new BinaryProgressReporter());
		ProgressReporters.add(e);
		metadata.history().addExecution(e);
		Progress.pushExecution(this);
	}

	@Override
	public void postprocess(Object output) {
		// Log a new execution
		OpExecution e = ProgressReporters.remove();
		e.recordCompletion(output);
		metadata.history().logOutput(e, output);
		Progress.popExecution();
	}

}
