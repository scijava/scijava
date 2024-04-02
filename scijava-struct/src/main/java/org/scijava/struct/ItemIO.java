/*
 * #%L
 * A library for building and introspecting structs.
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

package org.scijava.struct;

/**
 * Defines the input/output type of a struct {@link Member}.
 *
 * @author Curtis Rueden
 * @author David Kolb
 */
public enum ItemIO {
		/**
		 * The item is an input for the operation.
		 */
		INPUT,
		/**
		 * The item is an output for the operation.
		 */
		OUTPUT,
		/**
		 * The item is a hybrid input/output for the operation, whose structure is
		 * fixed but whose content will be populated during execution of the
		 * operation.
		 *
		 * @see org.scijava.param.Container
		 * @see org.scijava.ops.function.Computers
		 */
		CONTAINER,
		/**
		 * The item is a hybrid input/output for the operation, whose current value
		 * will be used as input to the computation, but subsequently overwritten
		 * with a result.
		 *
		 * @see org.scijava.param.Mutable
		 * @see org.scijava.ops.function.Inplaces
		 */
		MUTABLE,
		/**
		 * The type of the item should be inferred automatically during
		 * structification. For instance, for methods of functional interfaces, the
		 * types can be inferred as follows: the return type (if not {@code void})
		 * is {@code ItemIO.OUTPUT} and method parameters are typically
		 * {@code ItemIO.INPUT}, although method parameters could be annotated to
		 * indicate they should be treated as {@code ItemIO.CONTAINER} or
		 * {@code ItemIO.MUTABLE} type instead.
		 */
		AUTO,
		/**
		 * The type of the item is none of the above.
		 */
		NONE
}
