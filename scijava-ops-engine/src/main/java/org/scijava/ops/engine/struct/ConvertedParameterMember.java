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

package org.scijava.ops.engine.struct;

import java.lang.reflect.Type;

import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;
import org.scijava.struct.Structs;

/**
 * {@link Member} whose {@link Type} has been converted into another
 * {@link Type}
 *
 * @author Gabriel Selzer
 * @param <T>
 */
public class ConvertedParameterMember<T> implements Member<T> {

	final Member<T> original;
	final Type newType;
	final ItemIO ioType;

	public ConvertedParameterMember(Member<T> original,
		FunctionalMethodType newType)
	{
		this.original = original;
		this.newType = newType.type();
		this.ioType = newType.itemIO();
	}

	public static <M> ConvertedParameterMember<M> from(Member<M> original,
		FunctionalMethodType newType)
	{
		return new ConvertedParameterMember<>(original, newType);
	}

	@Override
	public String key() {
		return original.key();
	}

	@Override
	public String description() {
		return original.description();
	}

	@Override
	public Type type() {
		return newType;
	}

	@Override
	public ItemIO getIOType() {
		return ioType;
	}

	@Override
	public boolean isRequired() {
		return original.isRequired();
	}

	@Override
	public String toString() {
		return Structs.toString(this);
	}
}
