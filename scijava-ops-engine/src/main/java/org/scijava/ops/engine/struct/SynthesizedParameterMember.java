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

package org.scijava.ops.engine.struct;

import java.lang.reflect.Type;

import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;
import org.scijava.struct.Structs;

/**
 * {@link Member} synthesized using constructor arguments
 *
 * @author Gabriel Selzer
 * @param <T>
 */
public class SynthesizedParameterMember<T> implements Member<T> {

	/** {@link FunctionalMethodType} describing this Member */
	private final FunctionalMethodType fmt;

	/** Name of the parameter */
	private final String name;

	private final String description;

	private final boolean isRequired;

	public SynthesizedParameterMember(final FunctionalMethodType fmt,
		final String name, final boolean isRequired, final String description)
	{
		this.fmt = fmt;
		this.name = name;
		this.isRequired = isRequired;
		this.description = description;
	}

	// -- Member methods --

	@Override
	public String key() {
		return name;
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public Type type() {
		return fmt.type();
	}

	@Override
	public ItemIO getIOType() {
		return fmt.itemIO();
	}

	@Override
	public boolean isRequired() {
		return isRequired;
	}

	@Override
	public String toString() {
		return Structs.toString(this);
	}
}
