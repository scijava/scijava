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

import java.util.List;

import org.scijava.priority.Priority;

/**
 * A language scripts can be written in.
 * <p>
 * Contribute one through {@link java.util.ServiceLoader}. The JSR-223 engines
 * on the classpath are already contributed - see {@code JsrLanguages} - so a
 * language with a {@code javax.script} engine needs nothing at all; this
 * interface exists for the ones that do not, GraalVM's polyglot languages and
 * Appose's out-of-process interpreters being the cases in view.
 * </p>
 *
 * @author Curtis Rueden
 */
public interface ScriptLanguage {

	/** Gets this language's name, as a user would say it: "Groovy". */
	String name();

	/** Gets the file extensions scripts in this language use, without dots. */
	List<String> extensions();

	/**
	 * Starts a session: an interpreter with its own state, in which a script
	 * runs and its functions can afterwards be called.
	 */
	ScriptSession start();

	/** Sorts languages claiming the same extension, highest first. */
	default double priority() {
		return Priority.NORMAL;
	}
}
