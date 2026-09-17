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

import org.scijava.priority.Priority;

/**
 * Prepares a run before it happens: filling in inputs, checking preconditions,
 * harvesting values from the user.
 * <p>
 * Contribute one as a plugin:
 * </p>
 *
 * <pre>
 * &#64;Plugin(type = Preprocessor.class)
 * public class MyPreprocessor implements Preprocessor { ... }
 * </pre>
 * <p>
 * A preprocessor may stop the run by calling
 * {@link Execution#decline(String)}; later preprocessors are then skipped.
 * </p>
 * <p>
 * Implementations should be stateless: the container constructs one per run,
 * and a caller may supply a chain containing several instances of one class.
 * </p>
 *
 * @author Curtis Rueden
 */
public interface Preprocessor {

	/** Prepares the given run. */
	void process(Execution execution);

	/** Sorts preprocessors, highest first. See {@link Priority}. */
	default double priority() {
		return Priority.NORMAL;
	}
}
