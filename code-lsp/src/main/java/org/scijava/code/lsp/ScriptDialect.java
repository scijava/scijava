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

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.scijava.plugin.SciJavaPlugin;
import org.scijava.script.ScriptLanguage;

/**
 * A {@link SciJavaPlugin} that knows how to turn the scripts of a SciJava
 * language into plain code of an LSP language (a {@link ScriptDocument}), for
 * language servers of that language to analyze (see
 * {@link LanguageServerService#session}). E.g. Appose-Python scripts become
 * plain Python, their parameters declared as NumPy arrays and the like.
 * <p>
 * So script languages need not offer language servers themselves: servers
 * know their language, not SciJava scripts (see {@link LanguageServerPlugin}).
 * </p>
 * <p>
 * A dialect supplies the declarations, rather than translating scripts: a
 * {@link ScriptDocument} translates every script the same way, prepending the
 * dialect's {@link #header}, and blanking in place the lines SciJava removes
 * before a script runs (e.g. its {@code #@} lines). So, for every language,
 * positions in a script and in its document differ by just the header's
 * length, which is how a session maps servers' answers back to the script
 * (and leaves out those about the header); and that mapping is written, and
 * tested, once. A language whose scripts need more (e.g. wrapping in a class)
 * would extend that rule in {@link ScriptDocument}, for all languages.
 * </p>
 *
 * @author Gabriel Selzer
 * @see ScriptDocument
 */
public interface ScriptDialect extends SciJavaPlugin {

	/** Whether this turns scripts of the given SciJava language into code. */
	boolean supports(ScriptLanguage language);

	/**
	 * Gets the LSP language (e.g. {@code python}) of the code scripts become:
	 * whose servers analyze them (see {@link LanguageServerService#launch}).
	 */
	// TODO: Consider whether this is necessary: could the script language name
	// it, or servers be found some other way (e.g. by the dialect's class)?
	String languageId();

	/**
	 * Gets where a script runs, as far as its text says (e.g. an Appose
	 * environment it declares), to analyze it there. Asked before each request
	 * about the script, as its text may change that; its servers are kept one
	 * per environment (see
	 * {@link org.scijava.code.lsp.server.RoutingLanguageServer}). The default knows
	 * nothing: {@link Environment#NONE}.
	 *
	 * @param text The script, as written (e.g. with its {@code #@script} line,
	 *          which its servers do not see).
	 * @param file The script's file, if it has one (not if e.g. unsaved).
	 */
	// TODO: Consider whether this is necessary: so far only Appose-Python
	// scripts say where they run, and dialects otherwise only translate.
	default Environment environment(final String text,
		final Optional<File> file)
	{
		return Environment.NONE;
	}

	/**
	 * Gets the code declaring a script's parameters with the types they have
	 * when it runs, for a {@link ScriptDocument} to prepend to the script: e.g.
	 * {@code import numpy; image: numpy.ndarray} for an image in CPython. It
	 * may also import what the declarations need, and declare other variables
	 * scripts can use (e.g. Appose's {@code task}); parameters without a useful
	 * type may be left out.
	 *
	 * @param parameters The script's parameters (see
	 *          {@link LanguageServerService#scriptParameters}), by name, with
	 *          their Java types, in order.
	 */
	Header header(Map<String, Class<?>> parameters);

	/**
	 * One line of code to prepend to a script (see {@link #header}), e.g.
	 * statements joined by {@code ;}.
	 */
	final class Header {

		private final String code;
		private final Set<String> hiddenNames;

		/**
		 * @param code The code: one line, without a line break (the
		 *          {@link ScriptDocument} adds one after it), or empty if none.
		 * @param hiddenNames The names to leave out of completion: those the
		 *          code binds only to declare the parameters, e.g.
		 *          {@code numpy} for {@code import numpy}.
		 * @throws IllegalArgumentException If the code has a line break.
		 */
		public Header(final String code, final Collection<String> hiddenNames) {
			if (code.indexOf('\n') >= 0 || code.indexOf('\r') >= 0) {
				throw new IllegalArgumentException(
					"A header is one line, without line breaks: " + code);
			}
			this.code = code;
			this.hiddenNames = Collections.unmodifiableSet(new LinkedHashSet<>(
				hiddenNames));
		}

		/** The code: one line, without a line break; or empty if none. */
		public String code() {
			return code;
		}

		/**
		 * The names to leave out of completion: those the code binds only to
		 * declare the parameters, e.g. {@code numpy} for {@code import numpy}
		 * (unless the script itself mentions them, see
		 * {@link ScriptDocument#hiddenNames()}).
		 */
		public Set<String> hiddenNames() {
			return hiddenNames;
		}
	}
}
