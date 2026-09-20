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

import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.scijava.Context;
import org.scijava.log.LogLevel;
import org.scijava.log.LogService;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptInfo;
import org.scijava.script.ScriptModule;
import org.scijava.script.process.ParameterScriptProcessor;
import org.scijava.service.Service;

/**
 * Default {@link CodeCompletionService} implementation.
 *
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultCodeCompletionService extends
	AbstractSingletonService<CodeCompleterPlugin> implements CodeCompletionService
{

	/** Number of distinct parameter declarations to remember. */
	private static final int CACHE_SIZE = 16;

	/** Parameter declaration lines &rarr; parsed parameters, in LRU order. */
	private final Map<String, Map<String, Class<?>>> parameterCache =
		new LinkedHashMap<String, Map<String, Class<?>>>(CACHE_SIZE, 0.75f, true)
		{

			@Override
			protected boolean removeEldestEntry(
				final Map.Entry<String, Map<String, Class<?>>> eldest)
			{
				return size() > CACHE_SIZE;
			}
		};

	// -- CodeCompletionService methods --

	@Override
	public Map<String, Class<?>> scriptParameters(final String text) {
		if (text == null) return Collections.emptyMap();
		final String declarations = parameterLines(text);
		if (declarations.isEmpty()) return Collections.emptyMap();
		synchronized (parameterCache) {
			Map<String, Class<?>> params = parameterCache.get(declarations);
			if (params == null) {
				params = parseParameters(declarations);
				parameterCache.put(declarations, params);
			}
			return params;
		}
	}

	// -- Helper methods --

	/**
	 * Extracts the lines of the script that {@link ParameterScriptProcessor}
	 * would treat as parameter declarations: {@code #@} lines anywhere, plus
	 * old-style {@code // @} lines in the script header. Completion happens on
	 * every keystroke, but these lines rarely change, so they make a good cache
	 * key.
	 */
	private static String parameterLines(final String text) {
		final StringBuilder sb = new StringBuilder();
		boolean header = true;
		for (final String line : text.split("\r?\n")) {
			if (line.startsWith("#@")) sb.append(line).append("\n");
			else if (header) {
				if (line.matches("^[^\\w]*[^\\w#]@.*")) {
					sb.append(line).append("\n");
				}
				else if (line.matches(".*\\w.*")) header = false;
			}
		}
		return sb.toString();
	}

	/** Parses the given parameter declarations, quietly. */
	private Map<String, Class<?>> parseParameters(final String declarations) {
		final Map<String, Class<?>> params = new LinkedHashMap<>();
		try {
			final Context context = context();
			final ParameterScriptProcessor processor = new ParameterScriptProcessor();
			context.inject(processor);
			// Half-typed declarations are expected while editing: don't warn.
			// NB: A log level configured for this source still takes precedence.
			final LogService log = context.getService(LogService.class);
			if (log != null) {
				processor.setLogger(log.subLogger("code-completion", LogLevel.NONE));
			}
			// NB: Run only the parameter processor, not the full processing chain.
			final ScriptInfo info = new ScriptInfo(context, "completion",
				new StringReader(declarations))
			{

				@Override
				public void parseParameters() {
					clearParameters();
					processor.begin(this);
					for (final String line : declarations.split("\n")) {
						processor.process(line);
					}
					processor.end();
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
			// NB: Services needed for parameter parsing are unavailable, or the
			// declarations are too malformed to process. Fall through.
		}
		// Declared variables whose types could not be resolved are still known.
		for (final String line : declarations.split("\n")) {
			final String name = declaredName(line);
			if (name != null) params.putIfAbsent(name, Object.class);
		}
		return Collections.unmodifiableMap(params);
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
