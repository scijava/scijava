/*
 * #%L
 * Running things that declare their inputs and outputs.
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

package org.scijava.execute;

/**
 * A named piece of behavior belonging to an executable: a callback run when a
 * parameter changes, a validator, a function generating parameters.
 * <p>
 * The point of naming behavior rather than reflecting a Java method is that a
 * script has no Java methods. SciJava Common's callbacks, initializers and
 * validaters were method-name strings resolved through Java reflection, which
 * is exactly why they were never finished for scripts. An
 * {@link ExecutableInstance} resolves a name however its own kind of code
 * does: a Java class by reflecting a method, a script by asking its engine for
 * a function.
 * </p>
 *
 * @author Curtis Rueden
 * @see ExecutableInstance#behavior(String)
 */
@FunctionalInterface
public interface Behavior {

	/**
	 * Invokes this behavior.
	 *
	 * @param args the arguments, if it takes any
	 * @return whatever it returns, or null
	 * @throws BehaviorException if it fails
	 */
	Object invoke(Object... args);
}
