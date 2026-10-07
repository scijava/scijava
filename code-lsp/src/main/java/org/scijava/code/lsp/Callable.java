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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionItemLabelDetails;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.SignatureInformation;
import org.scijava.code.lsp.RatedSignatureInformation.Fit;

/**
 * Something to call (a method, function or constructor), as in-process
 * language servers describe it: its name, its parameters (with their types,
 * where known) and its return type. As a completion, it is a snippet with a
 * tab stop per parameter ({@code max(${1:a}, ${2:b})}) and the typed
 * parameters as label details ({@code (double a, double b)}); as a
 * signature, {@code max(double a, double b) -> double}.
 *
 * @author Gabriel Selzer
 */
public final class Callable {

	/** A parameter: its name, and its type (or null if unknown). */
	public static final class Param {

		private final String name;
		private final String type;

		public Param(final String name, final String type) {
			this.name = name;
			this.type = type;
		}

		public String name() {
			return name;
		}

		/** The type (e.g. {@code double} or {@code java.lang.String}), or null. */
		public String type() {
			return type;
		}

		/** E.g. {@code double a}, or {@code a} if the type is unknown. */
		@Override
		public String toString() {
			return type == null ? name : type + " " + name;
		}
	}

	private final String name;
	private final List<Param> params;
	private final String returnType;
	private CompletionItemKind kind = CompletionItemKind.Method;
	private String detail;

	/**
	 * @param name The name (as inserted, e.g. {@code max}).
	 * @param params Its parameters.
	 * @param returnType Its return type, or null if unknown (or none).
	 */
	public Callable(final String name, final List<Param> params,
		final String returnType)
	{
		this.name = name;
		this.params = params == null ? Collections.emptyList() : Collections
			.unmodifiableList(new ArrayList<>(params));
		this.returnType = returnType;
	}

	/** Sets the kind of completion (default: method). */
	public Callable kind(final CompletionItemKind k) {
		kind = k;
		return this;
	}

	/** Sets a one-line summary (e.g. a full signature). */
	public Callable detail(final String d) {
		detail = d;
		return this;
	}

	public String name() {
		return name;
	}

	public List<Param> params() {
		return params;
	}

	public String returnType() {
		return returnType;
	}

	/** The parameter types, e.g. for sorting overloads. */
	public String parameterTypes() {
		final StringBuilder sb = new StringBuilder();
		for (final Param p : params) {
			if (sb.length() > 0) sb.append(',');
			sb.append(p.type());
		}
		return sb.toString();
	}

	/** As a completion: a snippet with a tab stop per parameter. */
	public CompletionItem toItem() {
		final CompletionItem item = new CompletionItem(name);
		item.setKind(kind);
		item.setDetail(detail);
		final StringBuilder snippet = new StringBuilder(escape(name)).append('(');
		for (int i = 0; i < params.size(); i++) {
			if (i > 0) snippet.append(", ");
			final String p = params.get(i).name() != null ? params.get(i).name()
				: "arg" + i;
			snippet.append("${").append(i + 1).append(':').append(escape(p)).append(
				'}');
		}
		item.setInsertText(snippet.append(')').toString());
		item.setInsertTextFormat(InsertTextFormat.Snippet);
		item.setFilterText(name);
		final CompletionItemLabelDetails details = new CompletionItemLabelDetails();
		details.setDetail(parameterList());
		details.setDescription(returnType);
		item.setLabelDetails(details);
		return item;
	}

	/** As a signature, rated. */
	public SignatureInformation toSignature(final Fit fit) {
		final List<ParameterInformation> infos = new ArrayList<>();
		for (final Param p : params) infos.add(new ParameterInformation(p
			.toString()));
		final String label = name + parameterList() + (returnType == null ? ""
			: " -> " + returnType);
		return new RatedSignatureInformation(label, infos, fit);
	}

	/** E.g. {@code (double a, double b)}. */
	public String parameterList() {
		final StringBuilder sb = new StringBuilder("(");
		for (int i = 0; i < params.size(); i++) {
			if (i > 0) sb.append(", ");
			sb.append(params.get(i));
		}
		return sb.append(')').toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof Callable)) return false;
		final Callable c = (Callable) o;
		return name.equals(c.name) && parameterList().equals(c.parameterList()) &&
			Objects.equals(returnType, c.returnType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, parameterList(), returnType);
	}

	@Override
	public String toString() {
		return name + parameterList();
	}

	/** Escapes a snippet's special characters. */
	static String escape(final String s) {
		return s.replace("\\", "\\\\").replace("$", "\\$").replace("}", "\\}");
	}
}
