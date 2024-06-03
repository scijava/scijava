/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ops.engine.matcher.impl;

import java.lang.reflect.Type;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInstance;
import org.scijava.ops.api.RichOp;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.ops.engine.MatchingConditions;
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
	private final MatchingConditions conditions;

	public boolean record = true;

	public AbstractRichOp(final OpInstance<T> instance, final OpEnvironment env,
		final MatchingConditions conditions)
	{
		this.instance = instance;
		this.env = env;
		this.conditions = conditions;
	}

	@Override
	public OpEnvironment env() {
		return env;
	}

	@Override
	public Hints hints() {
		return conditions.hints();
	}

	@Override
	public String name() {
		return conditions.request().name();
	}

	@Override
	public OpInstance<T> instance() {
		return instance;
	}

	@Override
	public void preprocess(Object... inputs) {
		if (hints().contains(BaseOpHints.Progress.TRACK)) {
			Progress.register(this, conditions.request().name());
		}
		else {
			Progress.ignore();
		}
	}

	@Override
	public void postprocess(Object output) {
		if (!hints().contains(BaseOpHints.History.IGNORE)) {
			env.history().logOutput(this, output);
		}
		Progress.complete();
	}

	@Override
	public String toString() {
		return instance().toString();
	}

}
