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
import org.scijava.struct.StructInstance;

/**
 * An {@link Executable} backed by a Java class whose {@link Parameter} fields
 * declare its parameters, and which implements {@link Runnable}.
 *
 * @author Curtis Rueden
 */
public class JavaExecutable implements Executable {

	private final Class<? extends Runnable> type;
	private final Struct struct;

	public JavaExecutable(final Class<? extends Runnable> type) {
		this.type = type;
		this.struct = Executables.struct(type);
	}

	@Override
	public String name() {
		return type.getName();
	}

	@Override
	public Struct struct() {
		return struct;
	}

	@Override
	public ExecutableInstance create() {
		final Runnable object = instantiate();
		final StructInstance<?> parameters = struct.createInstance(object);
		return new ExecutableInstance() {

			@Override
			public Executable executable() {
				return JavaExecutable.this;
			}

			@Override
			public StructInstance<?> parameters() {
				return parameters;
			}

			@Override
			public void run() {
				object.run();
			}
		};
	}

	/** Gets the class this describes. */
	public Class<? extends Runnable> type() {
		return type;
	}

	private Runnable instantiate() {
		try {
			return type.getDeclaredConstructor().newInstance();
		}
		catch (final ReflectiveOperationException exc) {
			throw new IllegalStateException("Cannot construct " + type.getName() + //
				". Does it have a no-argument constructor, and does its module" + //
				" declare `opens " + type.getPackageName() + //
				" to org.scijava.execute;`?", exc);
		}
	}
}
