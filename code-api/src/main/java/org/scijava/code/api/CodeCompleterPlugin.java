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

import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.RichPlugin;
import org.scijava.plugin.SingletonPlugin;
import org.scijava.script.ScriptLanguage;

/**
 * A discoverable {@link CodeCompleter} for one or more script languages.
 * <p>
 * Implementations are annotated with @{@link Plugin}(type =
 * CodeCompleterPlugin.class) and discovered by the {@link CodeCompletionService}.
 * By default a plugin claims the language whose name matches the plugin's
 * {@link Plugin#name() name} attribute (case-insensitively); override
 * {@link #supports(ScriptLanguage)} for finer control (e.g. to support a family
 * of related languages).
 * </p>
 * <p>
 * Because completion lives behind this plugin rather than on
 * {@link ScriptLanguage} itself, a language adapter need not know anything about
 * completion, and third parties can contribute completion for languages they do
 * not own.
 * </p>
 *
 * @author Curtis Rueden
 * @see CodeCompletionService
 */
public interface CodeCompleterPlugin extends CodeCompleter, RichPlugin,
	SingletonPlugin
{

	/**
	 * Whether this plugin provides completion for the given language.
	 * <p>
	 * The default matches the language name against this plugin's {@code name}
	 * attribute, ignoring case.
	 * </p>
	 */
	default boolean supports(final ScriptLanguage language) {
		if (language == null) return false;
		final PluginInfo<?> info = getInfo();
		final String pluginName = info == null ? null : info.getName();
		return pluginName != null && //
			pluginName.equalsIgnoreCase(language.getLanguageName());
	}
}
