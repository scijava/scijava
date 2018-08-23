/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package org.scijava.ops.base;

import java.lang.reflect.Type;

import org.scijava.util.Types;

public final class TypeUtils {

	private TypeUtils() {
		// NB: prevent instantiation of utility class.
	}

	/**
	 * Discerns whether it would be legal to assign a reference of type source
	 * to a reference of type target. Works around possible bug in
	 * {@link org.scijava.util.Types#isAssignable(Type, Type)}, which returns
	 * false if one wants to assign primitives to their wrappers and the other
	 * way around.
	 * 
	 * @param from
	 *            The type from which assignment is desired.
	 * @param to
	 *            The type to which assignment is desired.
	 * @return Whether the type 'from' is assignable to the type 'to'
	 */
	public static boolean isAssignable(final Type from, final Type to) {
		if (from instanceof Class && to instanceof Class) {
			return Types.isAssignable(Types.box((Class<?>) from), Types.box((Class<?>) to));
		}
		return Types.isAssignable(from, to);
	}
}
