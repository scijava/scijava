/*
 * #%L
 * SciJava Log 2: SciJava plugins for logging.
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

package org.scijava.log2;

/**
 * Base class for {@link LoggerFactory} implementations.
 *
 * @author Johannes Schindelin
 * @author Curtis Rueden
 * @author Matthias Arzt
 */
@IgnoreAsCallingClass
public abstract class AbstractLoggerFactory implements LoggerFactory {

	private final int currentLevel = levelFromEnvironment();

	// -- constructor --

	@Override
	public Logger create() {
		return new RootLogger(currentLevel);
	}

	abstract void messageLogged(LogMessage message);

	// -- Helper methods -- 
	private int levelFromEnvironment() {
		// use the default, which is INFO unless the DEBUG env. variable is set
		return System.getenv("DEBUG") == null ? LogLevel.INFO : LogLevel.DEBUG;
	}

	// -- Helper classes --

	@IgnoreAsCallingClass
	private class RootLogger extends DefaultLogger {
		public RootLogger() {
			this(LogLevel.NONE);
		}

		public RootLogger(int level) {
			super(AbstractLoggerFactory.this::messageLogged, LogSource.newRoot(),
				level);
		}
	}
}
