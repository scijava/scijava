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

/**
 * A single code completion suggestion.
 * <p>
 * This is a toolkit-agnostic data structure: it carries everything a UI needs to
 * present and apply a suggestion, without depending on any particular widget
 * toolkit (Swing, JavaFX, etc.). Editors adapt {@link Completion}s into their own
 * native completion objects.
 * </p>
 * <p>
 * Instances are immutable; build them with {@link #builder(String)}.
 * </p>
 *
 * @author Curtis Rueden
 * @see CodeCompleter
 * @see CompletionResult
 */
public final class Completion {

	/** Broad category of a {@link Completion}, useful for icons and sorting. */
	public enum Kind {
		METHOD, FIELD, CLASS, PACKAGE, KEYWORD, VARIABLE, IMPORT, SNIPPET, OTHER
	}

	/**
	 * An additional text edit to apply elsewhere in the document when a
	 * completion is accepted, beyond inserting the completion itself. The
	 * canonical use is auto-importing: accepting a class name also inserts an
	 * {@code import} statement near the top of the file.
	 * <p>
	 * Offsets are relative to the document text as seen in the originating
	 * {@link CompletionRequest}. To remain valid after the primary insertion,
	 * every additional edit must lie at or before the result's
	 * {@link CompletionResult#replaceStart() replace start} (i.e. before the
	 * completion point); edits after it are not supported.
	 * </p>
	 */
	public static final class TextEdit {

		private final int start;
		private final int end;
		private final String newText;

		public TextEdit(final int start, final int end, final String newText) {
			this.start = start;
			this.end = end;
			this.newText = newText;
		}

		/** Convenience for a pure insertion at the given offset. */
		public static TextEdit insert(final int offset, final String text) {
			return new TextEdit(offset, offset, text);
		}

		/** Start offset of the replaced span. */
		public int start() {
			return start;
		}

		/** End offset of the replaced span (equals {@link #start()} for inserts). */
		public int end() {
			return end;
		}

		/** Replacement text. */
		public String newText() {
			return newText;
		}
	}

	/** A single parameter of a callable {@link Completion} (method, function). */
	public static final class Parameter {

		private final String name;
		private final String type;

		public Parameter(final String name, final String type) {
			this.name = name;
			this.type = type;
		}

		/** The parameter name, or {@code null} if unknown. */
		public String name() {
			return name;
		}

		/** The parameter type (a human-readable type name), or {@code null}. */
		public String type() {
			return type;
		}

		@Override
		public String toString() {
			if (type == null) return name == null ? "?" : name;
			return name == null ? type : type + " " + name;
		}
	}

	private final String insertionText;
	private final String displayText;
	private final String summary;
	private final String description;
	private final Kind kind;
	private final double relevance;
	private final List<Parameter> parameters;
	private final String returnType;
	private final String declaringClass;
	private final String docURL;
	private final List<TextEdit> additionalEdits;

	private Completion(final Builder b) {
		this.insertionText = b.insertionText;
		this.displayText = b.displayText == null ? b.insertionText : b.displayText;
		this.summary = b.summary;
		this.description = b.description;
		this.kind = b.kind;
		this.relevance = b.relevance;
		this.parameters = b.parameters == null ? Collections.emptyList()
			: Collections.unmodifiableList(new ArrayList<>(b.parameters));
		this.returnType = b.returnType;
		this.declaringClass = b.declaringClass;
		this.docURL = b.docURL;
		this.additionalEdits = b.additionalEdits == null ? Collections.emptyList()
			: Collections.unmodifiableList(new ArrayList<>(b.additionalEdits));
	}

	/** The text to insert (replacing the request's matched span). */
	public String insertionText() {
		return insertionText;
	}

	/** The label to display in the completion popup. */
	public String displayText() {
		return displayText;
	}

	/** A short one-line summary (e.g. a method signature), or {@code null}. */
	public String summary() {
		return summary;
	}

	/** A longer description, which may be HTML, or {@code null}. */
	public String description() {
		return description;
	}

	/** The kind of program element this completion represents. */
	public Kind kind() {
		return kind;
	}

	/** Relevance score; higher sorts first. Defaults to {@code 0}. */
	public double relevance() {
		return relevance;
	}

	/** Parameters, for callable completions; empty if none or unknown. */
	public List<Parameter> parameters() {
		return parameters;
	}

	/** Return type of a callable completion, or {@code null}. */
	public String returnType() {
		return returnType;
	}

	/** Fully qualified name of the declaring class, or {@code null}. */
	public String declaringClass() {
		return declaringClass;
	}

	/** A URL pointing at documentation for this completion, or {@code null}. */
	public String docURL() {
		return docURL;
	}

	/** Extra edits to apply on acceptance (e.g. auto-imports); never null. */
	public List<TextEdit> additionalEdits() {
		return additionalEdits;
	}

	/** True iff this completion represents a callable with parameters. */
	public boolean isCallable() {
		return kind == Kind.METHOD || !parameters.isEmpty();
	}

	@Override
	public String toString() {
		return displayText;
	}

	// -- Builder --

	/** Creates a builder for a completion with the given insertion text. */
	public static Builder builder(final String insertionText) {
		return new Builder(insertionText);
	}

	/** Convenience for a plain completion with no metadata. */
	public static Completion of(final String insertionText) {
		return builder(insertionText).build();
	}

	/** Fluent builder for {@link Completion}. */
	public static final class Builder {

		private final String insertionText;
		private String displayText;
		private String summary;
		private String description;
		private Kind kind = Kind.OTHER;
		private double relevance = 0;
		private List<Parameter> parameters;
		private String returnType;
		private String declaringClass;
		private String docURL;
		private List<TextEdit> additionalEdits;

		private Builder(final String insertionText) {
			this.insertionText = insertionText;
		}

		public Builder displayText(final String v) {
			this.displayText = v;
			return this;
		}

		public Builder summary(final String v) {
			this.summary = v;
			return this;
		}

		public Builder description(final String v) {
			this.description = v;
			return this;
		}

		public Builder kind(final Kind v) {
			this.kind = v;
			return this;
		}

		public Builder relevance(final double v) {
			this.relevance = v;
			return this;
		}

		public Builder parameters(final List<Parameter> v) {
			this.parameters = v;
			return this;
		}

		public Builder returnType(final String v) {
			this.returnType = v;
			return this;
		}

		public Builder declaringClass(final String v) {
			this.declaringClass = v;
			return this;
		}

		public Builder docURL(final String v) {
			this.docURL = v;
			return this;
		}

		public Builder additionalEdits(final List<TextEdit> v) {
			this.additionalEdits = v;
			return this;
		}

		public Completion build() {
			return new Completion(this);
		}
	}
}
