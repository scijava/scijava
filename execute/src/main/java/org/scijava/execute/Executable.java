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

import org.scijava.struct.Struct;

/**
 * A description of something runnable: what parameters it has, and how to
 * make one ready to run.
 * <p>
 * This is deliberately <em>not</em> "a Java class with annotated fields". A
 * script declares its parameters in its header, and an entry in a catalogue
 * may declare them in a file; both are executables, and neither has an
 * {@code @Parameter} field anywhere. Keeping this interface free of Java
 * reflection is what admits them.
 * </p>
 * <p>
 * NB: SciJava Common called this {@code ModuleInfo}. "Module" now means a JPMS
 * module, so the name had to change regardless of taste.
 * </p>
 *
 * @author Curtis Rueden
 * @see Executables#of(Class)
 */
public interface Executable {

	/** An identifier for this executable, unique among its kind. */
	String name();

	/** Describes the parameters: their names, types and direction. */
	Struct struct();

	/**
	 * Creates one ready to run.
	 * <p>
	 * Each call yields a fresh instance: parameter values are per-run state, so
	 * two concurrent runs must not share one.
	 * </p>
	 */
	ExecutableInstance create();
}
