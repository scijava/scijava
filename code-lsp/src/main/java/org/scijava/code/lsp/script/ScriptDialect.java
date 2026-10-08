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

package org.scijava.code.lsp.script;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * What a script language knows about turning a SciJava script into plain code
 * of that language (a {@link ScriptDocument}): how its script parameters look
 * at run time, how to declare them, and where the script runs.
 * <p>
 * For example, CPython scripts run via Appose receive images as NumPy arrays,
 * declared {@code image: numpy.ndarray}; Groovy scripts receive the Java
 * objects themselves, declared {@code net.imglib2.img.Img image;}.
 * </p>
 *
 * @author Gabriel Selzer
 * @see ScriptDocument
 */
public interface ScriptDialect {

	/**
	 * Gets the type a script parameter of the given Java type has in a running
	 * script, as written in this language, e.g. {@code numpy.ndarray} for an
	 * image in CPython; or null if it has no useful type (the parameter is then
	 * not declared).
	 */
	String runtimeType(Class<?> type);

	/**
	 * Declares a variable, e.g. {@code image: numpy.ndarray} (Python) or
	 * {@code net.imglib2.img.Img image;} (Groovy), without a line break.
	 *
	 * @param name The variable's name.
	 * @param type Its type, as given by {@link #runtimeType}.
	 */
	String declare(String name, String type);

	/**
	 * Gets statements needed before the declarations, e.g. imports of the types
	 * they use ({@code import numpy}). The default needs none.
	 *
	 * @param types The types declared, as given by {@link #runtimeType}.
	 */
	default List<String> imports(final Collection<String> types) {
		return Collections.emptyList();
	}

	/**
	 * Gets declarations of other variables scripts can use, e.g. Appose's
	 * {@code task}. The default declares none.
	 */
	default List<String> extraDeclarations() {
		return Collections.emptyList();
	}

	/**
	 * Gets the separator between statements on one line, e.g. {@code "; "}
	 * (the default).
	 */
	default String separator() {
		return "; ";
	}

	/**
	 * Whether a line of the script is not of this language, and must be blanked
	 * (e.g. {@code #@} lines for languages whose comments start otherwise). The
	 * default blanks none.
	 */
	default boolean blanks(final String line) {
		return false;
	}
}
