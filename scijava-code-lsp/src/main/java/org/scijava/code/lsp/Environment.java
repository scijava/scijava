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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.script.ScriptContext;


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

	/** Path of the interpreter or executable running the script, if known. */
	private final Optional<String> interpreter;
	/**
	 * Extra places imports are resolved from, in order: e.g. directories added
	 * to Python's {@code sys.path}, or a JVM classpath. Each server reads them as
	 * its language does. Never null.
	 */
	private final List<String> paths;
	private final Optional<ScriptContext> live;

	/**
	 * No particular environment: no interpreter, no extra paths, and no live
	 * interpreter. Others are made from it, e.g.
	 * {@code new Environment().withInterpreter(python)}.
	 */
	public Environment() {
		this(Optional.empty(), Collections.emptyList(), Optional.empty());
	}

	/**
	 * Since different environments may or may not have any of these,
	 * a builder pattern is nice here.
	 */
	private Environment(final Optional<String> interpreter,
		final List<String> paths, final Optional<ScriptContext> live)
	{
		this.interpreter = interpreter;
		this.paths = paths;
		this.live = live;
	}

	/** Path of the interpreter or executable running the script, if known. */
	public Optional<String> interpreter() {
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

	/** The state of the interpreter the code is typed into, if any. */
	public Optional<ScriptContext> live() {
		return live;
	}

	/**
	 * A copy of this environment, whose code is parsed by the given interpreter
	 * or executable.
	 *
	 * @param path Path of the interpreter or executable.
	 */
	public Environment withInterpreter(final String path) {
		return new Environment(Optional.of(path), paths, live);
	}

	/**
	 * A copy of this environment, whose imports are also resolved from the
	 * given places, in order. Language-dependent.
	 */
	public Environment withPaths(final List<String> places) {
		return new Environment(interpreter, List.copyOf(places), live);
	}

	/**
	 * A copy of this environment, for code typed into the given interpreter
	 * (e.g. a REPL's).
	 */
	public Environment withLive(final ScriptContext context) {
		return new Environment(interpreter, paths, Optional.of(context));
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof Environment)) return false;
		final Environment that = (Environment) o;
		// NB: Live interpreters are the same only if they are the same one.
		return interpreter.equals(that.interpreter) && paths.equals(that.paths) &&
			live.orElse(null) == that.live.orElse(null);
	}

	@Override
	public int hashCode() {
		return Objects.hash(interpreter, paths, System.identityHashCode(live
			.orElse(null)));
	}

	@Override
	public String toString() {
		return "Environment[interpreter=" + interpreter.orElse("none") +
			", paths=" + paths + (live.isPresent() ? ", live" : "") + "]";
	}
}
