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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Where a script runs, as far as analyzing it needs to know: the interpreter
 * (or other executable) that runs it, extra paths to search for modules or
 * packages, and classpath entries. Every part is optional.
 * <p>
 * For example, a CPython script run via Appose has its environment's Python
 * as interpreter, and a directory of packages installed for completion (see
 * dependency overlays) as an extra search path.
 * </p>
 *
 * @author Gabriel Selzer
 * @see ScriptDocument
 */
public final class EnvironmentContext {

	/** No particular environment. */
	public static final EnvironmentContext EMPTY = new EnvironmentContext(null,
		Collections.emptyList(), Collections.emptyList());

	private final String interpreter;
	private final List<String> searchPaths;
	private final List<String> classpath;

	/**
	 * @param interpreter Path of the interpreter or executable, or null.
	 * @param searchPaths Extra paths to search for modules, e.g. for Python.
	 * @param classpath Classpath entries, e.g. for JVM languages.
	 */
	public EnvironmentContext(final String interpreter,
		final List<String> searchPaths, final List<String> classpath)
	{
		this.interpreter = interpreter;
		this.searchPaths = searchPaths == null ? Collections.emptyList()
			: Collections.unmodifiableList(new ArrayList<>(searchPaths));
		this.classpath = classpath == null ? Collections.emptyList() : Collections
			.unmodifiableList(new ArrayList<>(classpath));
	}

	/** Path of the interpreter or executable running the script, or null. */
	public String interpreter() {
		return interpreter;
	}

	/** Extra paths to search for modules or packages; never null. */
	public List<String> searchPaths() {
		return searchPaths;
	}

	/** Classpath entries; never null. */
	public List<String> classpath() {
		return classpath;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof EnvironmentContext)) return false;
		final EnvironmentContext that = (EnvironmentContext) o;
		return Objects.equals(interpreter, that.interpreter) && searchPaths.equals(
			that.searchPaths) && classpath.equals(that.classpath);
	}

	@Override
	public int hashCode() {
		return Objects.hash(interpreter, searchPaths, classpath);
	}

	@Override
	public String toString() {
		return "EnvironmentContext[interpreter=" + interpreter + ", searchPaths=" +
			searchPaths + ", classpath=" + classpath + "]";
	}
}
