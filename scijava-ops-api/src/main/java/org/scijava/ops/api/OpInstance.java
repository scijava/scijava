/*-
 * #%L
 * Public interfaces for the SciJava Ops framework.
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
import java.util.Objects;

import org.scijava.types.GenericTyped;

/**
 * An instance of an {@link OpInfo}. They can be constructed directly, but are
 * easily generated from {@link InfoTree}s.
 * <p>
 * Each {@link OpInstance} has an Op and its corresponding {@link OpInfo}.
 * </p>
 *
 * @author Gabriel Selzer
 */
public class OpInstance<T> implements GenericTyped {

	private final T op;
	private final InfoTree info;
	private final Type reifiedType;

	public OpInstance(final T op, final InfoTree backingInfo,
		final Type reifiedType)
	{
		this.op = op;
		this.info = backingInfo;
		this.reifiedType = reifiedType;
	}

	public static <T> OpInstance<T> of(T op, InfoTree backingInfo,
		final Type reifiedType)
	{
		return new OpInstance<>(op, backingInfo, reifiedType);
	}

	public T op() {
		return op;
	}

	public InfoTree infoTree() {
		return info;
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof OpInstance)) return false;
		OpInstance<?> thatInstance = (OpInstance<?>) that;
		boolean infosEqual = infoTree().equals(thatInstance.infoTree());
		boolean objectsEqual = op().equals(thatInstance.op());
		boolean typesEqual = getType().equals(thatInstance.getType());
		return infosEqual && objectsEqual && typesEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(infoTree(), op(), getType());
	}

	@Override
	public Type getType() {
		return reifiedType;
	}

	@Override
	public String toString() {
		return infoTree().info().toString();
	}

}
