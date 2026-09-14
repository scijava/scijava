/*-
 * #%L
 * Core API for SciJava code intelligence features.
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

package org.scijava.code.api;

import java.util.Collections;
import java.util.List;

/**
 * The result of a {@link CodeCompleter} query: the list of suggestions plus the
 * offset in the source text at which the matched span begins.
 * <p>
 * {@link #replaceStart()} tells the editor which characters the chosen
 * {@link Completion#insertionText()} should replace — i.e. the start of the
 * partial token the user has already typed. The end of that span is the request
 * caret offset.
 * </p>
 *
 * @author Curtis Rueden
 * @see CodeCompleter
 */
public final class CompletionResult {

	/** A result with no suggestions. */
	public static final CompletionResult EMPTY =
		new CompletionResult(Collections.emptyList(), 0);

	private final List<Completion> completions;
	private final int replaceStart;
	private final ParameterChoices parameterChoices;

	public CompletionResult(final List<Completion> completions,
		final int replaceStart)
	{
		this(completions, replaceStart, null);
	}

	public CompletionResult(final List<Completion> completions,
		final int replaceStart, final ParameterChoices parameterChoices)
	{
		this.completions = Collections.unmodifiableList(completions);
		this.replaceStart = replaceStart;
		this.parameterChoices = parameterChoices;
	}

	/** The suggested completions, ordered by descending relevance. */
	public List<Completion> completions() {
		return completions;
	}

	/** The offset in the source text where the replaced span begins. */
	public int replaceStart() {
		return replaceStart;
	}

	/**
	 * A provider of candidate values for the parameters of callable completions
	 * in this result, or {@code null} if parameter assistance is unsupported.
	 */
	public ParameterChoices parameterChoices() {
		return parameterChoices;
	}

	/** True iff there are no suggestions. */
	public boolean isEmpty() {
		return completions.isEmpty();
	}
}
