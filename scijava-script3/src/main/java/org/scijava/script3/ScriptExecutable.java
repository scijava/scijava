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

import java.util.Map;
import java.util.Optional;

import org.scijava.convert3.Converters;
import org.scijava.execute.Behavior;
import org.scijava.execute.Executable;
import org.scijava.execute.ExecutableInstance;
import org.scijava.execute.Parameters;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;

/**
 * A script, as something that declares its inputs and outputs.
 * <p>
 * This is the whole point of the scripting layer: a script is an
 * {@link Executable} like any other, so it runs through the same
 * {@code Runner}, is preprocessed by the same preprocessors, harvests its
 * inputs through the same dialog, and can sit in a menu beside a Java command.
 * Nothing above this layer needs to know which it is.
 * </p>
 * <p>
 * NB: its parameters are built over a map rather than reflected off fields,
 * because a script has no fields - which is the same primitive a generated
 * parameter group needs, and why {@code Parameters.builder()} exists.
 * </p>
 *
 * @author Curtis Rueden
 */
public class ScriptExecutable implements Executable {

	private final String name;
	private final String code;
	private final ScriptLanguage language;
	private final ScriptHeader header;

	public ScriptExecutable(final String name, final String code,
		final ScriptLanguage language)
	{
		this.name = name;
		this.code = code;
		this.language = language;
		this.header = ScriptHeader.parse(code);
	}

	@Override
	public String name() {
		return name;
	}

	/** Gets the language this script is written in. */
	public ScriptLanguage language() {
		return language;
	}

	/** Gets what the script declares about itself. */
	public ScriptHeader header() {
		return header;
	}

	/** Gets the script's source. */
	public String code() {
		return code;
	}

	@Override
	public Struct struct() {
		return build().struct();
	}

	@Override
	public ExecutableInstance create() {
		return new Instance(build());
	}

	@Override
	public String toString() {
		return name + " [" + language.name() + "]";
	}

	// -- Helper methods --

	/** Builds the parameters the header declares, over a fresh map. */
	private StructInstance<Map<String, Object>> build() {
		final Parameters.Builder builder = Parameters.builder();
		for (final ScriptHeader.Parameter p : header.parameters()) {
			builder.add(p.name(), p.type(), p.io(), p.attrs(), defaultValue(p));
		}
		return builder.build();
	}

	/** Gets a parameter's declared default, converted to its type. */
	private static Object defaultValue(final ScriptHeader.Parameter p) {
		final String value = p.attrs().get("value");
		if (value == null) return null;
		return Converters.get().tryConvert(value, p.type()).orElse(null);
	}

	/** One run of the script: its own session, its own values. */
	private class Instance implements ExecutableInstance {

		private final StructInstance<Map<String, Object>> parameters;
		private ScriptSession session;

		Instance(final StructInstance<Map<String, Object>> parameters) {
			this.parameters = parameters;
		}

		@Override
		public Executable executable() {
			return ScriptExecutable.this;
		}

		@Override
		public StructInstance<?> parameters() {
			return parameters;
		}

		@Override
		public void run() {
			final ScriptSession s = session();
			s.eval(header.body());
			sync();
		}

		@Override
		public Optional<Behavior> behavior(final String name) {
			// NB: a name, resolved by the kind of code this is: here, a function
			// the script defined. The script must have been evaluated for its
			// functions to exist, so the session primes itself with the script on
			// first use.
			return session().function(name).map(behavior -> args -> {
				final Object result = behavior.invoke(args);
				// NB: and whatever the function changed comes back. A callback
				// exists precisely to change other parameters, so a behavior whose
				// effects stayed inside the interpreter would be useless.
				sync();
				return result;
			});
		}

		/**
		 * Copies the script's variables back into the parameters.
		 * <p>
		 * NB: converted, because a language has its own ideas about types -
		 * Groovy's {@code celsius * 9 / 5 + 32} is a {@code BigDecimal} - and the
		 * parameter's declared type is the authority on what its value is.
		 * </p>
		 */
		private void sync() {
			final ScriptSession s = session;
			if (s == null) return;
			for (final ScriptHeader.Parameter p : header.parameters()) {
				final Object value = s.get(p.name());
				if (value == null) continue;
				final Object converted = Converters.get().tryConvert(value,
					(java.lang.reflect.Type) p.type()).orElse(value);
				parameters.object().put(p.name(), converted);
			}
		}

		/** Gets this run's session, starting it on first use. */
		private synchronized ScriptSession session() {
			if (session == null) {
				session = language.start();
				bind();
				// NB: evaluated once so that its functions exist; running it again
				// is what run() does, with the values the user supplied.
				try {
					session.eval(header.body());
				}
				catch (final ScriptException exc) {
					// NB: a script that cannot run before its inputs are filled in
					// is ordinary -- it is about to be given them. Callbacks it
					// declared will simply not resolve until it does run.
				}
			}
			else bind();
			return session;
		}

		/**
		 * Hands the current parameter values to the session.
		 * <p>
		 * NB: every declared parameter is bound, including the ones with no
		 * value yet. A language that resolves names at run time - which is most
		 * of them - fails on an unbound name rather than seeing null, so a
		 * function referring to a parameter the user has not filled in would
		 * blow up instead of behaving.
		 * </p>
		 */
		private void bind() {
			final Map<String, Object> values = parameters.object();
			for (final ScriptHeader.Parameter p : header.parameters()) {
				session.put(p.name(), values.get(p.name()));
			}
		}
	}

}
