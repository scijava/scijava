/*-
 * #%L
 * Language Server Protocol bridge for SciJava code intelligence.
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

package org.scijava.code.lsp;

import java.util.List;

import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.SignatureInformation;

/**
 * A signature, rated by how well the arguments typed so far fit it: so that an
 * editor can mark the overloads that would need conversions, or do not fit.
 * <p>
 * This is a SciJava addition to LSP, for in-process use: over JSON-RPC, it is
 * an ordinary signature (servers list the best fits first).
 * </p>
 *
 * @author Gabriel Selzer
 */
public class RatedSignatureInformation extends SignatureInformation {

	/** How well arguments fit a signature. */
	public enum Fit {
			/** The arguments fit as they are. */
			MATCH,
			/** The arguments fit after a conversion (e.g. int to double). */
			CONVERSION,
			/** Not known (e.g. the arguments' types are unknown). */
			UNKNOWN,
			/** Some argument cannot fit. */
			MISMATCH
	}

	private final transient Fit fit;

	public RatedSignatureInformation(final String label,
		final List<ParameterInformation> parameters, final Fit fit)
	{
		super(label);
		setParameters(parameters);
		this.fit = fit == null ? Fit.UNKNOWN : fit;
	}

	/** How well the arguments typed so far fit this signature. */
	public Fit fit() {
		return fit;
	}

	/** The fit of any signature: {@link Fit#UNKNOWN} unless rated. */
	public static Fit fitOf(final SignatureInformation s) {
		return s instanceof RatedSignatureInformation
			? ((RatedSignatureInformation) s).fit() : Fit.UNKNOWN;
	}
}
