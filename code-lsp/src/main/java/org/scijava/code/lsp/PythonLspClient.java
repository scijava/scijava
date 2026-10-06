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

import java.util.ArrayList;
import java.util.List;

import org.scijava.code.api.Completion;

/**
 * Asks a Python language server (jedi-language-server) about scripts, and
 * understands its signatures, e.g.
 * {@code def dumps(obj, *, skipkeys: bool=False, **kw) -> str}.
 *
 * @author Gabriel Selzer
 */
public class PythonLspClient extends LspClient {

	public PythonLspClient(final LspServers servers) {
		super(servers);
	}

	@Override
	protected Completion.Builder callable(final String name,
		final String signature)
	{
		return parse(name, signature);
	}

	/**
	 * Parses a Python signature (as Jedi writes them): the parameters, with
	 * their annotations as types; and the return annotation. Returns null if it
	 * is not a signature.
	 */
	public static Completion.Builder parse(final String name,
		final String signature)
	{
		final int open = signature.indexOf('(');
		if (open < 0) return null;
		final int close = closing(signature, open);
		if (close < 0) return null;
		final List<Completion.Parameter> params = new ArrayList<>();
		for (final String part : split(signature.substring(open + 1, close))) {
			final String p = part.trim();
			// NB: Bare * and / only mark keyword-only and positional-only ones.
			if (p.isEmpty() || p.equals("*") || p.equals("/")) continue;
			final int colon = topLevel(p, ':'), eq = topLevel(p, '=');
			String paramName = p, type = null;
			if (colon >= 0 && (eq < 0 || colon < eq)) {
				paramName = p.substring(0, colon);
				type = p.substring(colon + 1, eq < 0 ? p.length() : eq).trim();
			}
			else if (eq >= 0) paramName = p.substring(0, eq);
			paramName = paramName.trim().replaceFirst("^\\*{1,2}", "");
			params.add(new Completion.Parameter(paramName, type == null || type
				.isEmpty() ? null : type));
		}
		final String rest = signature.substring(close + 1).trim();
		final String returns = rest.startsWith("->") ? rest.substring(2).trim()
			: null;
		return Completion.builder(name).parameters(params).returnType(
			returns == null || returns.isEmpty() ? null : returns);
	}

	// -- Helper methods --

	/** The index of the bracket closing the one at {@code open}, or -1. */
	private static int closing(final String s, final int open) {
		int depth = 0;
		char quote = 0;
		for (int i = open; i < s.length(); i++) {
			final char c = s.charAt(i);
			if (quote != 0) {
				if (c == '\\') i++;
				else if (c == quote) quote = 0;
			}
			else if (c == '\'' || c == '"') quote = c;
			else if (c == '(' || c == '[' || c == '{') depth++;
			else if (c == ')' || c == ']' || c == '}') {
				if (--depth == 0) return i;
			}
		}
		return -1;
	}

	/** Splits at the commas outside brackets and strings. */
	private static List<String> split(final String s) {
		final List<String> parts = new ArrayList<>();
		int start = 0;
		while (true) {
			final int comma = topLevel(s.substring(start), ',');
			if (comma < 0) break;
			parts.add(s.substring(start, start + comma));
			start += comma + 1;
		}
		parts.add(s.substring(start));
		return parts;
	}

	/** The index of the first {@code target} outside brackets and strings. */
	private static int topLevel(final String s, final char target) {
		int depth = 0;
		char quote = 0;
		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			if (quote != 0) {
				if (c == '\\') i++;
				else if (c == quote) quote = 0;
			}
			else if (c == '\'' || c == '"') quote = c;
			else if (c == '(' || c == '[' || c == '{') depth++;
			else if (c == ')' || c == ']' || c == '}') depth--;
			else if (c == target && depth == 0) return i;
		}
		return -1;
	}
}
