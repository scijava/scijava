/*-
 * #%L
 * SciJava Operations API: Outward-facing Interfaces used by the SciJava Operations framework.
 * %%
 * Copyright (C) 2021 - 2023 SciJava developers.
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

package org.scijava.ops.api;

import java.lang.reflect.Type;

import org.scijava.types.GenericTyped;

/**
 * An {@link OpInstance} with state
 * <p>
 * Each {@link RichOp} has <b>one</b> {@link OpInstance}, and <b>one</b>
 * </p>
 * 
 * @author Gabriel Selzer
 * @param <T> the functional {@link Type} of the wrapped Op
 */
public interface RichOp<T> extends GenericTyped {

	OpInstance<T> instance();

	OpEnvironment env();

	Hints hints();

	default T op() {
		return instance().op();
	}

	@Override
	default Type getType() {
		return instance().getType();
	}

	default InfoTree infoTree() {
		return instance().infoTree();
	}

	/**
	 * Returns this {@link RichOp} as its op interface {@link Type}
	 *
	 * @return this {@link RichOp} as the type of its op interface
	 */
	T asOpType();

	void preprocess(Object... inputs);

	void postprocess(Object output);

	boolean isRecordingExecutions();

	void recordExecutions(boolean record);
}
