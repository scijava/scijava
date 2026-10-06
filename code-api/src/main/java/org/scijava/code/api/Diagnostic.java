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

/**
 * A problem found in a script, e.g. a syntax error, as shown by a squiggle
 * under the code concerned. Found by {@link CodeCompleter#diagnose}.
 *
 * @author Gabriel Selzer
 */
public final class Diagnostic {

	/** How bad the problem is. */
	public enum Severity {
		ERROR, WARNING, INFO, HINT
	}

	private final int start;
	private final int end;
	private final Severity severity;
	private final String message;
	private final String source;

	/**
	 * @param start Where the code concerned starts, in the script.
	 * @param end Where it ends (exclusive).
	 * @param severity How bad the problem is.
	 * @param message What the problem is.
	 * @param source What found it (e.g. {@code jedi}), or null.
	 */
	public Diagnostic(final int start, final int end, final Severity severity,
		final String message, final String source)
	{
		this.start = start;
		this.end = Math.max(start, end);
		this.severity = severity == null ? Severity.ERROR : severity;
		this.message = message;
		this.source = source;
	}

	/** Where the code concerned starts, in the script. */
	public int start() {
		return start;
	}

	/** Where the code concerned ends (exclusive). */
	public int end() {
		return end;
	}

	/** How bad the problem is. */
	public Severity severity() {
		return severity;
	}

	/** What the problem is. */
	public String message() {
		return message;
	}

	/** What found it (e.g. {@code jedi}), or null. */
	public String source() {
		return source;
	}

	@Override
	public String toString() {
		return severity + " " + start + "-" + end + ": " + message;
	}
}
