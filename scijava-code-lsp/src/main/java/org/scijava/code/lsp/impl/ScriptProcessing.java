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

package org.scijava.code.lsp.impl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.scijava.Context;
import org.scijava.module.ModuleItem;
import org.scijava.script.ScriptInfo;
import org.scijava.script.ScriptModule;
import org.scijava.script.process.ParameterScriptProcessor;
import org.scijava.script.process.ScriptProcessor;
import org.scijava.script.process.ShebangScriptProcessor;

/**
 * Processes scripts as SciJava does before running them (see
 * {@link ScriptProcessor}), for sessions: the variables their parameters
 * define, and the lines SciJava removes (e.g. {@code #@} lines). Remembers
 * recent results, since sessions ask on every keystroke.
 * <p>
 * Only the processors that just read lines run:
 * {@link ShebangScriptProcessor} and {@link ParameterScriptProcessor}.
 * {@code #@script} directives are removed without running theirs, which
 * registers the script as a module.
 * </p>
 *
 * @author Gabriel Selzer
 */
final class ScriptProcessing {

	/** What SciJava makes of a script before running it. */
	static final class Processed {

		static final Processed NONE = new Processed(Collections.emptyMap(),
			Collections.emptySet());

		/** The variables its parameters define, by name, in order. */
		final Map<String, Class<?>> parameters;

		/** The lines (from 0) SciJava removes. */
		final Set<Integer> removedLines;

		private Processed(final Map<String, Class<?>> parameters,
			final Set<Integer> removedLines)
		{
			this.parameters = parameters;
			this.removedLines = removedLines;
		}
	}

	/** Number of distinct processed lines to remember. */
	private static final int CACHE_SIZE = 16;

	/** An old-style parameter, in a script's header: e.g. "// @String name". */
	private static final Pattern OLD_STYLE = Pattern.compile(
		"[^\\w]*[^\\w#]@.*");

	private static final Pattern WORD = Pattern.compile("\\w");

	/** A {@code #@script(...)} directive. */
	private static final Pattern DIRECTIVE = Pattern.compile(
		"#@script(?!\\w).*");

	/** The lines processed &rarr; what SciJava made of them, in LRU order. */
	private final Map<List<String>, Lines> cache =
		new LinkedHashMap<List<String>, Lines>(CACHE_SIZE, 0.75f, true)
		{

			@Override
			protected boolean removeEldestEntry(
				final Map.Entry<List<String>, Lines> eldest)
			{
				return size() > CACHE_SIZE;
			}
		};

	private final Supplier<Context> context;

	ScriptProcessing(final Supplier<Context> context) {
		this.context = context;
	}

	/** Processes a script, as SciJava does before running it. */
	Processed of(final String text) {
		if (text == null) return Processed.NONE;
		// NB: Only the lines SciJava's processors act on are processed: they
		// rarely change as a script is edited, so they make a good cache key;
		// and processing logs warnings about invalid parameters, e.g.
		// half-typed ones, which would otherwise be logged on every keystroke.
		final List<String> lines = new ArrayList<>();
		final List<Integer> lineNumbers = new ArrayList<>();
		final String[] all = text.split("\r?\n", -1);
		boolean header = true;
		for (int n = 0; n < all.length; n++) {
			final String line = all[n];
			final boolean shebang = n == 0 && line.startsWith("#!");
			final boolean processed = shebang || line.startsWith("#@") ||
				header && OLD_STYLE.matcher(line).matches();
			if (!processed && WORD.matcher(line).find()) header = false;
			if (processed) {
				lines.add(line);
				lineNumbers.add(n);
			}
		}
		if (lines.isEmpty()) return Processed.NONE;

		final Lines result;
		synchronized (cache) {
			result = cache.computeIfAbsent(lines, this::process);
		}
		final Set<Integer> removed = new LinkedHashSet<>();
		for (int i = 0; i < lines.size(); i++) {
			if (result.removed[i]) removed.add(lineNumbers.get(i));
		}
		return new Processed(result.parameters, Collections.unmodifiableSet(
			removed));
	}

	// -- Helper methods --

	/** What SciJava made of some lines. */
	private static final class Lines {

		private final Map<String, Class<?>> parameters;
		private final boolean[] removed;

		private Lines(final Map<String, Class<?>> parameters,
			final boolean[] removed)
		{
			this.parameters = parameters;
			this.removed = removed;
		}
	}

	/** Runs SciJava's script processors on the given lines. */
	private Lines process(final List<String> lines) {
		final Map<String, Class<?>> params = new LinkedHashMap<>();
		final boolean[] removed = new boolean[lines.size()];
		try {
			final Context context = this.context.get();
			final List<ScriptProcessor> processors = Arrays.asList(
				new ShebangScriptProcessor(), new ParameterScriptProcessor());
			for (final ScriptProcessor p : processors) context.inject(p);
			final ScriptInfo info = new ScriptInfo(context, "completion",
				new StringReader(String.join("\n", lines) + "\n"))
			{

				@Override
				public void parseParameters() {
					clearParameters();
					for (final ScriptProcessor p : processors) p.begin(this);
					for (int i = 0; i < lines.size(); i++) {
						// NB: Directives are removed, without running their
						// processor.
						if (DIRECTIVE.matcher(lines.get(i)).matches()) continue;
						String line = lines.get(i);
						for (final ScriptProcessor p : processors) {
							line = p.process(line);
						}
						removed[i] = line.isEmpty();
					}
					for (final ScriptProcessor p : processors) p.end();
				}
			};
			for (final ModuleItem<?> item : info.inputs()) {
				params.putIfAbsent(item.getName(), item.getType());
			}
			final boolean implicitResult = info.isReturnValueAppended();
			for (final ModuleItem<?> item : info.outputs()) {
				final boolean declared = !implicitResult || //
					!ScriptModule.RETURN_VALUE.equals(item.getName());
				if (declared) params.putIfAbsent(item.getName(), item.getType());
			}
		}
		catch (final RuntimeException exc) {
			// NB: Services needed for script processing are unavailable, or the
			// lines are too malformed to process. Fall through.
		}
		// NB: Parameters SciJava rejects, e.g. half-typed or of types it cannot
		// resolve, are likely being typed: the variables they declare are still
		// known, and their #@ lines removed all the same.
		for (int i = 0; i < lines.size(); i++) {
			final String line = lines.get(i);
			if (line.startsWith("#!")) continue; // shebang
			final String name = declaredName(line);
			if (name != null) params.putIfAbsent(name, Object.class);
			if (line.startsWith("#@")) removed[i] = true;
		}
		return new Lines(Collections.unmodifiableMap(params), removed);
	}

	/**
	 * Gets the variable name declared by a parameter line, without resolving its
	 * type, or null if the line does not declare one.
	 */
	private static String declaredName(final String line) {
		final String param = line.substring(line.indexOf('@') + 1);
		final int lParen = param.indexOf('(');
		final int rParen = param.lastIndexOf(')');
		final String cut = lParen >= 0 && rParen > lParen ? //
			param.substring(0, lParen) + param.substring(rParen + 1) : param;
		final String[] tokens = cut.trim().split("[ \t]+");
		if (tokens.length < 2) return null;
		final String name = tokens[tokens.length - 1];
		if (!Character.isJavaIdentifierStart(name.charAt(0))) return null;
		for (int i = 1; i < name.length(); i++) {
			if (!Character.isJavaIdentifierPart(name.charAt(i))) return null;
		}
		return name;
	}
}
