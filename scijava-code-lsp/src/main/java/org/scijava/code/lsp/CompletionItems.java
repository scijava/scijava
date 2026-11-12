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
	 * Builds a method completion from name and parameters.
	 *
	 * @param name The name of the method
	 * @param paramNames The names of the method's parameters, in order
	 * @param paramList An enclosed list of parameter names and types as
	 *          they'd appear in this method's programming language. In Java,
	 *          for example, {@code (double a, double b)}. In Python,
	 *          {@code (a: double, b: double)}
	 * @return a {@link CompletionItem} describing a method.
	 */
	public static CompletionItem method(final String name,
		final List<String> paramNames, final String paramList)
	{
		final CompletionItem item = new CompletionItem(name);
		// Kind
		item.setKind(CompletionItemKind.Method);
		// Label
		item.setLabel(name); // Should just be the name since we're adding details
		final CompletionItemLabelDetails details = new CompletionItemLabelDetails();
		details.setDetail(paramList);
		item.setLabelDetails(details);
		// InsertText
		final StringBuilder snippet = new StringBuilder(escape(name)).append('(');
		for (int i = 0; i < paramNames.size(); i++) {
			if (i > 0) snippet.append(", ");
			snippet.append("${").append(i + 1).append(':').append(escape(paramNames
				.get(i))).append('}');
		}
		item.setInsertText(snippet.append(')').toString());
		item.setInsertTextFormat(InsertTextFormat.Snippet);
		// FilterText
		item.setFilterText(item.getLabel());
		return item;
	}

	/**
	 * Completions for the public instance fields and methods of a Java class,
	 * as reached through an instance (e.g. {@code s.}), whose names start with
	 * a (case-insensitive) prefix, by reflection; methods as
	 * {@link #method(String, List, String) method snippets}, with Java-style
	 * parameter lists (e.g. {@code (double a, double b)}), and their
	 * signatures as their details.
	 *
	 * @param c The class.
	 * @param prefix The prefix; empty matches everything.
	 */
	public static List<CompletionItem> instanceMembers(final Class<?> c,
		final String prefix)
	{
		return members(c, prefix, false);
	}

	/**
	 * Completions for the public static fields and methods of a Java class,
	 * as reached through the class (e.g. {@code Math.}); otherwise as
	 * {@link #instanceMembers}.
	 *
	 * @param c The class.
	 * @param prefix The prefix; empty matches everything.
	 */
	public static List<CompletionItem> staticMembers(final Class<?> c,
		final String prefix)
	{
		return members(c, prefix, true);
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

	private static List<CompletionItem> members(final Class<?> c,
		final String prefix, final boolean staticMembers)
	{
		final List<CompletionItem> out = new ArrayList<>();
		if (c == null) return out;
		final String lp = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
		for (final Field f : c.getFields()) {
			if (Modifier.isStatic(f.getModifiers()) != staticMembers) continue;
			if (!f.getName().toLowerCase(Locale.ROOT).startsWith(lp)) continue;
			out.add(field(f));
		}
		for (final Method m : c.getMethods()) {
			if (Modifier.isStatic(m.getModifiers()) != staticMembers) continue;
			if (!m.getName().toLowerCase(Locale.ROOT).startsWith(lp)) continue;
			out.add(method(m));
		}
		return out;
	}

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
