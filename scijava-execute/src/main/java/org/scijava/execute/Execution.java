/*
 * #%L
 * Running things that declare their inputs and outputs.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.execute;

import java.util.Map;
import java.util.Optional;

import org.scijava.struct.StructInstance;

/**
 * One run in progress, handed to each {@link Preprocessor} and
 * {@link Postprocessor}.
 *
 * @author Curtis Rueden
 */
public interface Execution {

	/** Gets the thing being run, whose parameters and behaviors it carries. */
	ExecutableInstance executable();

	/** Gets its parameters, bound to their values. */
	StructInstance<?> instance();

	/**
	 * Declines this run: it will not happen, and the result will say so.
	 * <p>
	 * This is how a preprocessor reports that the user cancelled a dialog, or
	 * that a precondition is not met. It is not an error, and it is not
	 * {@linkplain java.util.concurrent.Future#cancel(boolean) cancellation} of
	 * work already under way - nothing has run yet.
	 * </p>
	 * <p>
	 * NB: SciJava Common put this on the preprocessor itself, as
	 * {@code isCanceled()}/{@code getCancelReason()}, which made preprocessors
	 * stateful and unsafe to share between runs. Telling the execution instead
	 * keeps them stateless, and lets the result record <em>which</em>
	 * preprocessor declined, even where two instances of one class are in the
	 * same chain.
	 * </p>
	 *
	 * @param reason why this run should not happen, for the user to read
	 */
	void decline(String reason);

	/** Gets whether this run has been declined. */
	boolean isDeclined();

	/** Gets why this run was declined, if it was. */
	Optional<String> reason();

	/**
	 * Gets the outputs produced so far. Empty until the object has run, so this
	 * is for postprocessors.
	 */
	Map<String, Object> outputs();
}
