/*
 * #%L
 * Running scripts as things that declare their inputs and outputs.
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

package org.scijava.script3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.scijava.execute.Behavior;

/**
 * A language backed by a {@code javax.script} engine.
 * <p>
 * NB: JSR-223 is the first backend rather than the only one. It is what
 * Groovy, Jython, JRuby, Clojure, BeanShell and the Fiji script editor already
 * speak, so it costs nothing and brings every existing language along; GraalVM
 * polyglot and Appose are further {@link ScriptLanguage}s, not replacements
 * for this one.
 * </p>
 *
 * @author Curtis Rueden
 */
public class JsrLanguage implements ScriptLanguage {

	private final ScriptEngineFactory factory;

	public JsrLanguage(final ScriptEngineFactory factory) {
		this.factory = factory;
	}

	/** Gets a language for every JSR-223 engine on the classpath. */
	public static List<ScriptLanguage> discover() {
		final List<ScriptLanguage> languages = new ArrayList<>();
		for (final ScriptEngineFactory factory : new ScriptEngineManager()
			.getEngineFactories())
		{
			languages.add(new JsrLanguage(factory));
		}
		return languages;
	}

	@Override
	public String name() {
		return factory.getLanguageName();
	}

	@Override
	public List<String> extensions() {
		return List.copyOf(factory.getExtensions());
	}

	@Override
	public ScriptSession start() {
		return new JsrSession(factory.getScriptEngine());
	}

	@Override
	public String toString() {
		return name() + " " + factory.getLanguageVersion();
	}

	/** A session backed by a {@link ScriptEngine}. */
	private static class JsrSession implements ScriptSession {

		private final ScriptEngine engine;

		JsrSession(final ScriptEngine engine) {
			this.engine = engine;
		}

		@Override
		public void put(final String name, final Object value) {
			engine.put(name, value);
		}

		@Override
		public Object get(final String name) {
			return engine.get(name);
		}

		@Override
		public Object eval(final String code) {
			try {
				return engine.eval(code);
			}
			catch (final javax.script.ScriptException exc) {
				throw new ScriptException("Script failed: " + exc.getMessage(), exc);
			}
		}

		@Override
		public Optional<Behavior> function(final String name) {
			if (!(engine instanceof Invocable)) return Optional.empty();
			final Invocable invocable = (Invocable) engine;
			return Optional.of(args -> {
				try {
					return invocable.invokeFunction(name, args);
				}
				catch (final NoSuchMethodException exc) {
					// NB: the script declared a callback it did not define. That is
					// the script's bug, and saying so beats doing nothing quietly.
					throw new ScriptException("No such function in script: " + name,
						exc);
				}
				catch (final javax.script.ScriptException exc) {
					throw new ScriptException("Script function '" + name +
						"' failed: " + exc.getMessage(), exc);
				}
			});
		}
	}
}
