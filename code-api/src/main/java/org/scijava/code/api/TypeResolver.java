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

/**
 * Determines the type of an expression, as written in the script being
 * completed. Front ends use it to see which overload of a callable
 * {@link Completion} the arguments typed so far are compatible with.
 * <p>
 * Like {@link ParameterChoices}, a {@link CodeCompleter} typically builds one
 * {@code TypeResolver} per {@link CompletionResult}, capturing the scope it
 * analyzed.
 * </p>
 *
 * @author Gabriel Selzer
 * @see CompletionResult#typeResolver()
 */
@FunctionalInterface
public interface TypeResolver {

	/**
	 * Gets the type of the given expression.
	 *
	 * @param expression source text of an expression, e.g. a function argument
	 * @return a fully qualified class name (e.g. {@code java.lang.String}), a
	 *         primitive type name (e.g. {@code double}), or {@code null} if the
	 *         type is unknown
	 */
	String typeOf(String expression);
}
