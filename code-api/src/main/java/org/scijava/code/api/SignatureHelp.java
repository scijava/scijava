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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The signatures of the call around the caret, e.g. the overloads of
 * {@code max} in {@code Math.max(1.5, |)}, each rated against the arguments
 * typed so far; plus which argument the caret is in. Computed by
 * {@link CodeCompleter#signatureHelp}; front ends render it, e.g. as a tooltip
 * listing the signatures, matching ones first.
 *
 * @author Gabriel Selzer
 * @see CodeCompleter#signatureHelp
 */
public final class SignatureHelp {

	/** No signatures: the caret is not in a call, or its callee is unknown. */
	public static final SignatureHelp NONE = new SignatureHelp(Collections
		.emptyList(), -1, -1);

	/** How well a signature fits the arguments typed so far. */
	public enum Fit {
			/** Every argument whose type is known can be passed as is. */
			MATCH,
			/** The arguments fit, but only after a widening conversion. */
			CONVERSION,
			/** Some argument cannot be passed (or there are too many). */
			MISMATCH,
			/** Not rated, e.g. no argument's type is known yet. */
			UNKNOWN
	}

	/** One signature of the callee, and how well it fits. */
	public static final class Signature {

		private final Completion callable;
		private final Fit fit;

		/**
		 * @param callable The signature: name, parameters, return type and
		 *          documentation, as for a callable completion.
		 * @param fit How well it fits the arguments typed so far.
		 */
		public Signature(final Completion callable, final Fit fit) {
			this.callable = callable;
			this.fit = fit == null ? Fit.UNKNOWN : fit;
		}

		/** The signature, as a callable completion. */
		public Completion callable() {
			return callable;
		}

		/** How well it fits the arguments typed so far. */
		public Fit fit() {
			return fit;
		}
	}

	private final List<Signature> signatures;
	private final int activeParameter;
	private final int callStart;
	private final CompletableFuture<SignatureHelp> update;

	/**
	 * @param signatures The callee's signatures, in the order to show them.
	 * @param activeParameter Index of the argument the caret is in, or -1.
	 * @param callStart Offset of the call's opening parenthesis, or -1.
	 */
	public SignatureHelp(final List<Signature> signatures,
		final int activeParameter, final int callStart)
	{
		this(signatures, activeParameter, callStart, null);
	}

	private SignatureHelp(final List<Signature> signatures,
		final int activeParameter, final int callStart,
		final CompletableFuture<SignatureHelp> update)
	{
		this.signatures = Collections.unmodifiableList(new ArrayList<>(
			signatures));
		this.activeParameter = activeParameter;
		this.callStart = callStart;
		this.update = update;
	}

	/** The callee's signatures, in the order the completer chose. */
	public List<Signature> signatures() {
		return signatures;
	}

	/** Index of the argument the caret is in, or -1 if unknown. */
	public int activeParameter() {
		return activeParameter;
	}

	/** Offset of the call's opening parenthesis, or -1 if unknown. */
	public int callStart() {
		return callStart;
	}

	/** True iff there are no signatures. */
	public boolean isEmpty() {
		return signatures.isEmpty();
	}

	/**
	 * Gets a copy which announces better help for the same request, to follow,
	 * as {@link CompletionResult#withUpdate} does for completions.
	 *
	 * @param future Completes with the better help (or with null, if there is
	 *          none after all).
	 */
	public SignatureHelp withUpdate(
		final CompletableFuture<SignatureHelp> future)
	{
		return new SignatureHelp(signatures, activeParameter, callStart, future);
	}

	/**
	 * Better help for the same request, still to come, or {@code null} if this
	 * help is final.
	 */
	public CompletableFuture<SignatureHelp> update() {
		return update;
	}
}
