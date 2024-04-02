/*-
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

import java.util.function.Supplier;

public interface MemberInstance<T> extends Supplier<T> {

	Member<T> member();

	default boolean isReadable() {
		return false;
	}

	default boolean isWritable() {
		return false;
	}

	/**
	 * Get's the value of the member.
	 *
	 * @return The value of the {@link Member} with the given key.
	 * @throws UnsupportedOperationException if the member is not readable (see
	 *           {@link #isReadable()}).
	 */
	@Override
	default T get() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the value of the member.
	 *
	 * @param value The value to set.
	 * @throws UnsupportedOperationException if the member is not writable (see
	 *           {@link #isWritable()}).
	 */
	default void set(Object value) {
		throw new UnsupportedOperationException();
	}

}
