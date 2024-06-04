/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.ops.engine;

import org.scijava.ops.api.OpRequest;
import org.scijava.ops.api.OpMatchingException;

/**
 * An {@link OpMatchingException} caused by another {@code OpMatchingException}
 * thrown when resolving dependencies. We illustrate the need for
 * {@code DependencyMatchingException} with an example: Suppose Op {@code A} has
 * an dependency on Op {@code B}, which in turn has a dependency on Op
 * {@code C}. Suppose further that {@code A} and {@code B} are found, but no
 * {@code C} is found to satisfy {@code B}. This will result in an
 * {@link OpMatchingException} thrown for Op {@code A}. We would want to know:
 * <ol>
 * <li>that the request for {@code A} was not fulfilled</li>
 * <li>that no match could be found for {@code C}, which was the cause for
 * (1)</li>
 * </ol>
 * This logic can be generalized for an arbitrarily long Op chain. TODO:
 * Consider supporting non-DependencyMatchingException {@code cause}s.
 *
 * @author Gabriel Selzer
 */
public class DependencyMatchingException extends OpMatchingException {

	public DependencyMatchingException(final String message) {
		super(message);
	}

	public static String message(String dependentOp, final String dependencyName,
		OpRequest dependency)
	{
		return "Could not instantiate dependency of: " + dependentOp +
			"\nDependency identifier: " + dependencyName + //
			"\n\nAttempted request:\n" + dependency;
	}

	public DependencyMatchingException(final String message,
		final DependencyMatchingException cause)
	{
		super(message, cause);
	}

	@Override
	public String getMessage() {
		if (getCause() == null) return super.getMessage();
		return super.getMessage() + "\n\nCause:\n\n" + getCause().getMessage();
	}
}
