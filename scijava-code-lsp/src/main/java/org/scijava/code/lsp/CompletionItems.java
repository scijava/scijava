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

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionItemLabelDetails;
import org.eclipse.lsp4j.InsertTextFormat;

/**
 * Helpers for building {@link CompletionItem}s.
 *
 * @author Gabriel Selzer
 */
public final class CompletionItems {

	private CompletionItems() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * Builds a completion that inserts a call to a method (or function, or
	 * constructor), from its name and its parameters.
	 * <p>
	 * LSP offers the parts, but leaves how to put them together to each
	 * server: its snippet syntax, for inserting a call with tab stops through
	 * its arguments; and its label details, whose {@code detail} it suggests
	 * for function signatures. Here, they are put together in one way, which
	 * clients can rely on:
	 * </p>
	 * <ul>
	 * <li>The insert text is a snippet calling the method, with a tab stop per
	 * parameter, holding the parameter's name as a placeholder, e.g.
	 * {@code max(${1:a}, ${2:b})}.</li>
	 * <li>The label details' {@code detail} is the parameter list: one entry
	 * per tab stop, in order, e.g. {@code (double a, double b)} or
	 * {@code (a: float, b: float = 0.0)}.</li>
	 * </ul>
	 * <p>
	 * The filter text is the method's name. (By LSP, clients filter on the
	 * label, which is the name, anyway; this guards against those filtering on
	 * the insert text.) The kind is {@link CompletionItemKind#Method}. Callers
	 * may change the kind, and add e.g. a detail (such as the full signature,
	 * with the return type), documentation, or the label details'
	 * {@code description} (such as a qualified name).
	 * </p>
	 *
	 * @param name The name, e.g. {@code max}.
	 * @param paramNames The parameters' names, as the tab stops show them.
	 * @param paramList The parameter list, as the language writes it: one
	 *          entry per name, in the same order, e.g.
	 *          {@code (double a, double b)}.
	 */
	public static CompletionItem methodSnippet(final String name,
		final List<String> paramNames, final String paramList)
	{
		final CompletionItem item = new CompletionItem(name);
		item.setKind(CompletionItemKind.Method);
		final StringBuilder snippet = new StringBuilder(escape(name)).append('(');
		for (int i = 0; i < paramNames.size(); i++) {
			if (i > 0) snippet.append(", ");
			snippet.append("${").append(i + 1).append(':').append(escape(paramNames
				.get(i))).append('}');
		}
		item.setInsertText(snippet.append(')').toString());
		item.setInsertTextFormat(InsertTextFormat.Snippet);
		item.setFilterText(name);
		final CompletionItemLabelDetails details = new CompletionItemLabelDetails();
		details.setDetail(paramList);
		item.setLabelDetails(details);
		return item;
	}

	/** Escapes a snippet's special characters. */
	private static String escape(final String s) {
		return s.replace("\\", "\\\\").replace("$", "\\$").replace("}", "\\}");
	}
}
