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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * What came of a run: its outputs, or why it did not happen.
 * <p>
 * NB: this is deliberately not itself a {@link java.util.concurrent.Future}.
 * A future's {@code isCancelled()} and {@code get()} are coupled by contract -
 * whenever the former is true the latter must throw - so a declined run
 * modelled as a cancelled future would be an exception again, which is what
 * returning a result avoids. The asynchrony lives in
 * {@code Future<ExecutionResult>}, and what happened lives here.
 * </p>
 *
 * @author Curtis Rueden
 */
public class ExecutionResult {

	private final Map<String, Object> outputs;
	private final String reason;
	private final Preprocessor declinedBy;

	private ExecutionResult(final Map<String, Object> outputs,
		final String reason, final Preprocessor declinedBy)
	{
		this.outputs = outputs;
		this.reason = reason;
		this.declinedBy = declinedBy;
	}

	/** Creates the result of a run that happened. */
	public static ExecutionResult completed(final Map<String, Object> outputs) {
		return new ExecutionResult(Map.copyOf(outputs), null, null);
	}

	/** Creates the result of a run that was declined before it happened. */
	public static ExecutionResult declined(final String reason,
		final Preprocessor declinedBy)
	{
		return new ExecutionResult(Collections.emptyMap(), reason, declinedBy);
	}

	/** Gets whether the run happened. */
	public boolean isCompleted() {
		return declinedBy == null && reason == null;
	}

	/** Gets whether the run was declined before it happened. */
	public boolean isDeclined() {
		return !isCompleted();
	}

	/** Gets why the run was declined, if it was, for the user to read. */
	public Optional<String> reason() {
		return Optional.ofNullable(reason);
	}

	/**
	 * Gets which preprocessor declined the run, if one did.
	 * <p>
	 * NB: the instance rather than its class, since a chain may hold several
	 * instances of one class and the useful question is which of them objected.
	 * </p>
	 */
	public Optional<Preprocessor> declinedBy() {
		return Optional.ofNullable(declinedBy);
	}

	/** Gets the outputs, by name. Empty if the run was declined. */
	public Map<String, Object> outputs() {
		return outputs;
	}

	@Override
	public String toString() {
		return isDeclined() ? "declined: " + reason : "completed: " + outputs;
	}
}
