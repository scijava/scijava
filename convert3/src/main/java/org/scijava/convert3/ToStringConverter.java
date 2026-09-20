/*
 * #%L
 * Converting a value to the type something else wants.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.convert3;

import java.lang.reflect.Type;

import org.scijava.common3.Types;
import org.scijava.priority.Priority;

/**
 * Converts anything to text, with {@link Object#toString()}.
 * <p>
 * NB: low priority, since it accepts everything. Anything more specific -
 * formatting a number, naming a file - should outrank it.
 * </p>
 *
 * @author Curtis Rueden
 */
public class ToStringConverter implements Converter<Object, String> {

	@Override
	public Type sourceType() {
		return Object.class;
	}

	@Override
	public Type destType() {
		return String.class;
	}

	@Override
	public boolean supports(final Object source, final Type dest) {
		return source != null && Types.raw(dest) == String.class;
	}

	@Override
	public Object convert(final Object source, final Type dest) {
		return source.toString();
	}

	@Override
	public double priority() {
		return Priority.LOW;
	}
}
