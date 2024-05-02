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

/**
 * Generic Ops utilities
 *
 * @author Gabriel Selzer
 */
public final class Ops {

	private Ops() {}

	/**
	 * Convenience function for determining whether {@code op} is a
	 * {@link RichOp}.
	 *
	 * @param op the Op
	 * @return true iff {@code op} is a {@link RichOp}
	 */
	public static boolean isRich(Object op) {
		return op instanceof RichOp;
	}

	/**
	 * Convenience function for getting the {@link RichOp} of {@code op}
	 *
	 * @param op the Op
	 * @return the {@link RichOp} wrapping {@code op}
	 * @param <T> the type of {@code op}
	 * @throws IllegalArgumentException when a {@link RichOp} cannot be obtained
	 *           for {@code op}
	 */
	@SuppressWarnings("unchecked")
	public static <T> RichOp<T> rich(T op) {
		if (!isRich(op)) {
			throw new IllegalArgumentException(op + " is not a RichOp!");
		}
		return (RichOp<T>) op;
	}

	/**
	 * Convenience function for getting the {@link InfoTree} behind {@code op}
	 *
	 * @param op the Op
	 * @return the {@link InfoTree} of {@code op}
	 * @throws IllegalArgumentException if {@code op} is not an Op
	 */
	public static InfoTree infoTree(Object op) {
		return rich(op).instance().infoTree();
	}

	/**
	 * Convenience function for getting the {@link OpInfo} of {@code op}
	 *
	 * @param op the Op
	 * @return the {@link OpInfo} that generated {@code op}
	 * @throws IllegalArgumentException if {@code op} is not an Op
	 */
	public static OpInfo info(Object op) {
		return infoTree(op).info();
	}

	/**
	 * Convenience function for getting the signature of {@code op}
	 *
	 * @param op the Op
	 * @return the signature of {@code op}, which can be used to completely
	 *         restore {@code op}
	 * @throws IllegalArgumentException if {@code op} is not an Op
	 */
	public static String signature(Object op) {
		return infoTree(op).signature();
	}

}
