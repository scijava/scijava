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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
	public static CompletionItem method(final String name,
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

	/**
	 * Completions for the public fields and methods of a Java class whose
	 * names start with a (case-insensitive) prefix, by reflection; methods as
	 * {@link #method(String, List, String) method snippets}, with Java-style
	 * parameter lists (e.g. {@code (double a, double b)}), and their
	 * signatures as their details.
	 *
	 * @param c The class.
	 * @param prefix The prefix; empty matches everything.
	 * @param staticOnly Whether to include only static members.
	 */
	public static List<CompletionItem> members(final Class<?> c,
		final String prefix, final boolean staticOnly)
	{
		final List<CompletionItem> out = new ArrayList<>();
		if (c == null) return out;
		final String lp = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
		for (final Field f : c.getFields()) {
			if (staticOnly && !Modifier.isStatic(f.getModifiers())) continue;
			if (!f.getName().toLowerCase(Locale.ROOT).startsWith(lp)) continue;
			out.add(field(f));
		}
		for (final Method m : c.getMethods()) {
			if (staticOnly && !Modifier.isStatic(m.getModifiers())) continue;
			if (!m.getName().toLowerCase(Locale.ROOT).startsWith(lp)) continue;
			out.add(method(m));
		}
		return out;
	}

	/**
	 * The name of a Java type, as code writes it and as these completions
	 * show it: its canonical name (e.g. {@code int[]}, or
	 * {@code java.util.Map.Entry}), or else (e.g. for local classes) its
	 * binary name.
	 */
	public static String typeName(final Class<?> c) {
		final String n = c.getCanonicalName();
		return n == null ? c.getName() : n;
	}

	// -- Helper methods --

	/** A completion for a field. */
	private static CompletionItem field(final Field f) {
		final CompletionItem item = new CompletionItem(f.getName());
		item.setKind(CompletionItemKind.Field);
		item.setInsertText(f.getName());
		item.setDetail(typeName(f.getType()) + " " + f.getName());
		return item;
	}

	/** A completion for a method, e.g. {@code max(double a, double b)}. */
	private static CompletionItem method(final Method m) {
		final List<String> names = new ArrayList<>();
		final List<String> params = new ArrayList<>();
		for (final java.lang.reflect.Parameter p : m.getParameters()) {
			names.add(p.getName());
			params.add(typeName(p.getType()) + " " + p.getName());
		}
		final CompletionItem item = method(m.getName(), names, "(" + String.join(
			", ", params) + ")");
		item.setDetail(signature(m));
		return item;
	}

	/** A readable method signature, e.g. {@code String concat(String)}. */
	private static String signature(final Method m) {
		final StringBuilder sb = new StringBuilder();
		sb.append(typeName(m.getReturnType())).append(' ').append(m.getName())
			.append('(');
		final Class<?>[] types = m.getParameterTypes();
		for (int i = 0; i < types.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append(typeName(types[i]));
		}
		return sb.append(')').toString();
	}

	/** Escapes a snippet's special characters. */
	private static String escape(final String s) {
		return s.replace("\\", "\\\\").replace("$", "\\$").replace("}", "\\}");
	}
}
