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

package org.scijava.ops.engine.exceptions.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.scijava.ops.engine.exceptions.InvalidOpException;

/**
 * Exception thrown when an Op written as a {@link Method} does not conform to a
 * functional {@link Type} (i.e. implementing an interface with a single
 * abstract {@link Method}). This is not allowed, as if there are multiple
 * methods, Ops will not know which method to call!
 *
 * @author Gabriel Selzer
 */
public class FunctionalTypeOpException extends InvalidOpException {

	/**
	 * Standard constructor.
	 *
	 * @param op An Op implementation
	 * @param fIface the {@link FunctionalInterface} that the Op should conform
	 *          to.
	 */
	public FunctionalTypeOpException(final Object op, final Type fIface) {
		super("The signature of method " + op +
			" does not conform to the op type " + fIface);
	}

	/**
	 * Constructor used when another {@link Exception} indicates an issue with the
	 * functional type
	 *
	 * @param op An Op implementation
	 * @param cause the {@link Throwable} identifying a bad Op type.
	 */
	public FunctionalTypeOpException(final Object op, final Throwable cause) {
		super("The signature of op " + op + " did not have a suitable Op type.",
			cause);
	}

}
