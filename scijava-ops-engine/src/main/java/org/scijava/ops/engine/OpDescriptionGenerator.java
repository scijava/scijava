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

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpRequest;
import org.scijava.priority.Prioritized;
import org.scijava.priority.Priority;

/**
 * An interface whose implementations are able to describe all the Ops in an
 * {@link OpEnvironment} that could satisfy an {@link OpRequest}.
 *
 * @author Gabriel Selzer
 */
public interface OpDescriptionGenerator extends
	Prioritized<OpDescriptionGenerator>
{

	public static final String NO_OP_MATCHES =
		"No Ops found matching this request.";

	/**
	 * Returns a {@link String} with a "simple" description for each Op in
	 * {@code env} matching {@code request}.
	 *
	 * @param env an {@link OpEnvironment} containing Ops that may match
	 *          {@code request}
	 * @param request an {@link OpRequest} to filter on.
	 * @return a {@link String} with a "simple" description for each Op in
	 *         {@code env} matching {@code request}.
	 */
	String simpleDescriptions(OpEnvironment env, OpRequest request);

	/**
	 * Returns a {@link String} with a "simple" description for each Op in
	 * {@code env} matching {@code request}.
	 *
	 * @param env an {@link OpEnvironment} containing Ops that may match
	 *          {@code request}
	 * @param request an {@link OpRequest} to filter on.
	 * @return a {@link String} with a "simple" description for each Op in
	 *         {@code env} matching {@code request}.
	 */
	String verboseDescriptions(OpEnvironment env, OpRequest request);

	default double getPriority() {
		return Priority.NORMAL;
	}

}
