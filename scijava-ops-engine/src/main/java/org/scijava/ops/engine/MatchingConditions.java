/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2025 SciJava developers.
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

import java.util.Objects;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpRequest;

public class MatchingConditions {

	private final OpRequest request;
	private final Hints hints;

	private MatchingConditions(OpRequest request, Hints hints) {
		this.request = request;
		this.hints = hints;
	}

	public static MatchingConditions from(OpRequest request, Hints h) {
        var hintCopy = h.copy();
		return new MatchingConditions(request, hintCopy);
	}

	public OpRequest request() {
		return request;
	}

	public Hints hints() {
		return hints;
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof MatchingConditions)) return false;
        var thoseConditions = (MatchingConditions) that;
		return request().equals(thoseConditions.request()) && hints().equals(
			thoseConditions.hints());
	}

	@Override
	public int hashCode() {
		return Objects.hash(request(), hints());
	}

	@Override
	public String toString() {
		return request().toString();
	}

}
