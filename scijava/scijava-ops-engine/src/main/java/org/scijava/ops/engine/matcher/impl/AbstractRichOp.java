
package org.scijava.ops.engine.matcher.impl;

import java.lang.reflect.Type;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInstance;
import org.scijava.ops.api.RichOp;
import org.scijava.progress.Progress;

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
	private final OpEnvironment env;
	private final Hints hints;

	public AbstractRichOp(final OpInstance<T> instance, final OpEnvironment env, final Hints hints) {
		this.instance = instance;
		this.env = env;
		this.hints = hints;

		this.env.history().logOp(this);
	}

	@Override
	public OpEnvironment env() {
		return env;
	}

	@Override
	public Hints hints() {
		return hints;
	}
	@Override
	public OpInstance<T> instance() {
		return instance;
	}

	@Override
	public void preprocess(Object... inputs) {
		Progress.register(this);
	}

	@Override
	public void postprocess(Object output) {
		if (env.history().attendingTo(this)) {
			env.history().logOutput(this, output);
		}
		Progress.complete();
	}

}
