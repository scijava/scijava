/*-
 * #%L
 * The public API of SciJava Ops.
 * %%
 * Copyright (C) 2021 - 2024 SciJava developers.
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

import org.scijava.common3.GenericTyped;

/**
 * An {@link OpInstance} with state
 *
 * @author Gabriel Selzer
 * @param <T> the functional {@link Type} of the wrapped Op
 */
public interface RichOp<T> extends GenericTyped {

	/**
	 * Gets the {@link OpInstance} of this {@link RichOp}
	 *
	 * @return the {@link OpInstance} of this {@link RichOp}
	 */
	OpInstance<T> instance();

	OpEnvironment env();

	Hints hints();

	String name();

	/**
	 * Getter for this {@link RichOp}'s <b>raw</b> Op instance
	 *
	 * @return the raw Op instance of this {@link RichOp}
	 */
	default T op() {
		return instance().op();
	}

	/**
	 * Getter for this {@link RichOp}'s functional {@link Type}
	 *
	 * @return the {@link Type} of this Op
	 */

	@Override
	default Type type() {
		return instance().type();
	}

	/**
	 * Getter for this {@link RichOp}'s {@link InfoTree}
	 *
	 * @return the {@link InfoTree} describing the construction of this Op
	 */
	default InfoTree infoTree() {
		return instance().infoTree();
	}

	/**
	 * Returns this {@link RichOp} as its op interface {@link Type}
	 *
	 * @return this {@link RichOp} as the type of its op interface
	 */
	T asOpType();

	/**
	 * Defines behavior performed by the Op before <em>each</em> execution.
	 *
	 * @param inputs the inputs to the Op's functional method
	 */
	default void preprocess(Object... inputs) {
		// By default, do nothing
	}

	/**
	 * Defines behavior performed by the Op after <em>each</em> execution.
	 *
	 * @param output the output of the Op's functional method
	 */
	default void postprocess(Object output) {
		// By default, do nothing
	}
}
