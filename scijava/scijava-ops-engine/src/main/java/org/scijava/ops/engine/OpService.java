/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2018 SciJava developers.
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

import org.scijava.ops.api.OpBuilder;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpHistory;
import org.scijava.service.SciJavaService;

/**
 * Service to provide a list of available ops structured in a prefix tree and to
 * search for ops matching specified types.
 *
 * @author Gabriel Selzer
 * @author David Kolb
 */
public interface OpService extends SciJavaService {

	/**
	 * Begins declaration of an Op matching request for locating an Op with a
	 * particular name. Additional criteria are specified as chained method calls
	 * on the returned {@link OpBuilder} object. See {@link OpBuilder} for
	 * examples.
	 * 
	 * @param opName The name of the op to be matched.
	 * @return An {@link OpBuilder} for refining the search criteria for an Op.
	 * @see OpBuilder
	 */
	public OpBuilder op(final String opName);

	/** Retrieves the motherlode of available ops. */
	public OpEnvironment env();

	/** Retrieves a log of all Ops executed */
	public OpHistory history();

}
