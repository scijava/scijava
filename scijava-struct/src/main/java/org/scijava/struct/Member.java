/*-
 * #%L
 * A lightweight framework for collecting Members
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

package org.scijava.struct;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.scijava.types.Types;

/**
 * One element (i.e. item/field/member) of a {@link Struct}.
 *
 * @author Curtis Rueden
 * @author Christian Dietz
 */
public interface Member<T> {

	/** Unique name of the member. */
	String getKey();

	/** Non-null description. The default description is an empty string */
	default String getDescription() {
		return "";
	}

	/**
	 * Gets the type of the member, including Java generic parameters.
	 *
	 * @see Field#getGenericType()
	 */
	// TODO: Use Type<T> or Nil<T> from new scijava-types.
	Type getType();

	/**
	 * Gets the {@link Class} of the member's type, or null if {@link #getType()}
	 * does not return a raw class.
	 */
	@SuppressWarnings("unchecked")
	default Class<T> getRawType() {
		return (Class<T>) Types.raw(getType());
	}

	/** Gets the input/output type of the member. */
	// TODO: fork ItemIO and rename to MemberIO (?)
	ItemIO getIOType();

	/** Gets whether the member is an input. */
	default boolean isInput() {
		return getIOType() == ItemIO.INPUT || getIOType() == ItemIO.CONTAINER ||
			getIOType() == ItemIO.MUTABLE;
	}

	/** Gets whether the member is an output. */
	default boolean isOutput() {
		return getIOType() == ItemIO.OUTPUT || getIOType() == ItemIO.CONTAINER ||
			getIOType() == ItemIO.MUTABLE;
	}

	default boolean isStruct() {
		return false;
	}

	default boolean isRequired() {
		return true;
	}

	default Struct childStruct() {
		return null;
	}

	default MemberInstance<T> createInstance(
		@SuppressWarnings("unused") Object o)
	{
		return () -> Member.this;
	}
}
