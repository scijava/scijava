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

import java.util.Map;

import org.scijava.plugin.SingletonService;
import org.scijava.script.ScriptLanguage;
import org.scijava.service.SciJavaService;

/**
 * Service that manages {@link CodeCompleterPlugin}s and resolves the best
 * {@link CodeCompleter} for a given {@link ScriptLanguage}.
 *
 * @author Curtis Rueden
 */
public interface CodeCompletionService extends
	SingletonService<CodeCompleterPlugin>, SciJavaService
{

	/** Fallback completer used when no language-specific plugin is available. */
	CodeCompleter DEFAULT_COMPLETER = new DefaultCodeCompleter();

	/**
	 * Gets the language-specific {@link CodeCompleterPlugin} for the given
	 * language, or {@code null} if none is registered. Plugins are consulted in
	 * priority order; the first that {@link CodeCompleterPlugin#supports supports}
	 * the language wins.
	 */
	default CodeCompleterPlugin getCompleterPlugin(final ScriptLanguage language) {
		if (language == null) return null;
		for (final CodeCompleterPlugin plugin : getInstances()) {
			if (plugin.supports(language)) return plugin;
		}
		return null;
	}

	/**
	 * Gets a {@link CodeCompleter} for the given language, never {@code null}.
	 * Returns the language-specific plugin if one is registered, otherwise the
	 * reflective {@link #DEFAULT_COMPLETER}.
	 */
	default CodeCompleter getCompleter(final ScriptLanguage language) {
		final CodeCompleterPlugin plugin = getCompleterPlugin(language);
		return plugin != null ? plugin : DEFAULT_COMPLETER;
	}

	/** Convenience: completes the given request via {@link #getCompleter}. */
	default CompletionResult complete(final CompletionRequest request) {
		return getCompleter(request.language()).complete(request);
	}

	/**
	 * Gets the SciJava script parameters declared in the given script text (e.g.
	 * {@code #@ String name}): the variables the framework injects as inputs, or
	 * harvests as outputs, when the script runs. Completers can treat these as
	 * predeclared variables.
	 * <p>
	 * Parsing is quiet, since half-typed declarations are expected while
	 * editing, and cached by the declaration lines, so calling this on every
	 * keystroke is cheap.
	 * </p>
	 *
	 * @param text The script text.
	 * @return Map of variable names to types, in declaration order. Variables
	 *         whose type cannot be resolved map to {@link Object}. Never null.
	 */
	Map<String, Class<?>> scriptParameters(String text);

	// -- SingletonService methods --

	@Override
	default Class<CodeCompleterPlugin> getPluginType() {
		return CodeCompleterPlugin.class;
	}
}
