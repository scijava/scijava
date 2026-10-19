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

import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.SimpleBindings;


/**
 * The context needed to analyze a block of code.
 * <p>
 * Server processes are kept one per environment (see
 * {@link LanguageServerService#launch}): e.g. jedi-language-server learns the
 * Python to analyze against only when it starts.
 * </p>
 *
 * @author Gabriel Selzer
 */
public final class Environment {

	/**
	 * The state of no interpreter, for code not typed into one: no variables,
	 * and none can be set.
	 */
	public static final ScriptContext NO_STATE = new NoState();

	/** No particular environment. */
	public static final Environment NONE = new Environment(null, null);

	private final String interpreter;
	private final List<String> paths;
	private final ScriptContext live;

	/**
	 * An environment for code not typed into an interpreter (see
	 * {@link #NO_STATE}).
	 *
	 * @param interpreter Path of the interpreter or executable parsing the code.
	 * @param paths Extra places imports are resolved from, in order. Language-dependent.
	 */
	public Environment(final String interpreter, final List<String> paths) {
		this(interpreter, paths, NO_STATE);
	}

	/**
	 * @param interpreter Path of the interpreter or executable parsing the code.
	 * @param paths Extra places imports are resolved from, in order. Language-dependent.
	 * @param live The state of the interpreter the code is typed into (e.g. a
	 *          REPL's), or {@link #NO_STATE}.
	 */
	public Environment(final String interpreter, final List<String> paths,
		final ScriptContext live)
	{
		this.interpreter = interpreter;
		this.paths = paths == null ? Collections.emptyList() : Collections
			.unmodifiableList(new ArrayList<>(paths));
		this.live = Objects.requireNonNull(live, "live");
	}

	/** Path of the interpreter or executable running the script, or null. */
	public String interpreter() {
		return interpreter;
	}

	/**
	 * Extra places imports are resolved from, in order: e.g. directories added
	 * to Python's {@code sys.path}, or a JVM classpath. Each server reads them as
	 * its language does. Never null.
	 */
	public List<String> paths() {
		return paths;
	}

	/**
	 * The state of the interpreter the code is typed into; or
	 * {@link #NO_STATE}, if none.
	 */
	public ScriptContext live() {
		return live;
	}

	/** A copy of this environment, for code typed into the given interpreter. */
	public Environment withLive(final ScriptContext context) {
		return context == live ? this : new Environment(interpreter, paths,
			context);
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof Environment)) return false;
		final Environment that = (Environment) o;
		// NB: Live interpreters are the same only if they are the same one.
		return Objects.equals(interpreter, that.interpreter) && paths.equals(
			that.paths) && live == that.live;
	}

	@Override
	public int hashCode() {
		return Objects.hash(interpreter, paths, System.identityHashCode(live));
	}

	@Override
	public String toString() {
		return "Environment[interpreter=" + interpreter + ", paths=" + paths +
			(live == NO_STATE ? "" : ", live") + "]";
	}

	/** The state of no interpreter: see {@link #NO_STATE}. */
	private static final class NoState implements ScriptContext {

		private static final Bindings NONE = new SimpleBindings(Collections
			.emptyMap());

		private static final Writer NO_OUTPUT = new Writer() {

			@Override
			public void write(final char[] buf, final int off, final int len) {}

			@Override
			public void flush() {}

			@Override
			public void close() {}
		};

		@Override
		public void setBindings(final Bindings bindings, final int scope) {
			throw unmodifiable();
		}

		@Override
		public Bindings getBindings(final int scope) {
			return NONE;
		}

		@Override
		public void setAttribute(final String name, final Object value,
			final int scope)
		{
			throw unmodifiable();
		}

		@Override
		public Object getAttribute(final String name, final int scope) {
			return null;
		}

		@Override
		public Object removeAttribute(final String name, final int scope) {
			return null;
		}

		@Override
		public Object getAttribute(final String name) {
			return null;
		}

		@Override
		public int getAttributesScope(final String name) {
			return -1;
		}

		@Override
		public Writer getWriter() {
			return NO_OUTPUT;
		}

		@Override
		public Writer getErrorWriter() {
			return NO_OUTPUT;
		}

		@Override
		public void setWriter(final Writer writer) {
			throw unmodifiable();
		}

		@Override
		public void setErrorWriter(final Writer writer) {
			throw unmodifiable();
		}

		@Override
		public Reader getReader() {
			return new StringReader("");
		}

		@Override
		public void setReader(final Reader reader) {
			throw unmodifiable();
		}

		@Override
		public List<Integer> getScopes() {
			return Arrays.asList(ENGINE_SCOPE, GLOBAL_SCOPE);
		}

		private static UnsupportedOperationException unmodifiable() {
			return new UnsupportedOperationException(
				"The state of no interpreter cannot be changed");
		}
	}
}
